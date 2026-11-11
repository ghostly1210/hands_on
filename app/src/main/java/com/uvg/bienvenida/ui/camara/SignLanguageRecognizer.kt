package com.uvg.bienvenida.ui.camara

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SignLanguageRecognizer(context: Context) : Closeable {

    data class Prediction(
        val label: String,
        val score: Float
    )

    private val appContext = context.applicationContext

    private var hands: HandLandmarker? = null
    private var pose: PoseLandmarker? = null
    private var interpreter: Interpreter? = null

    // Conservamos el buffer mientras exista el intérprete.
    private var modelBuffer: ByteBuffer? = null

    private val labels: List<String>
    private val frames = mutableListOf<FloatArray>()

    private var recording = false
    private var closed = false
    private var lastTimestamp = -1L
    private var framesWithHands = 0

    init {
        val text = appContext.assets
            .open("lensegua_labels.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONArray(text)
        labels = List(json.length()) { json.getString(it) }
    }

    // Llamar desde un hilo de fondo.
    @Synchronized
    fun startCapture() {
        check(!closed) { "El reconocedor está cerrado." }

        recording = false
        frames.clear()
        framesWithHands = 0

        try {
            initializeModels()

            // Nueva sesión de seguimiento para cada intento.
            hands?.close()
            hands = null
            pose?.close()
            pose = null

            hands = HandLandmarker.createFromOptions(
                appContext,
                HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder()
                            .setModelAssetPath("hand_landmarker.task")
                            .build()
                    )
                    .setRunningMode(RunningMode.VIDEO)
                    .setNumHands(2)
                    .build()
            )

            pose = PoseLandmarker.createFromOptions(
                appContext,
                PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder()
                            .setModelAssetPath("pose_landmarker.task")
                            .build()
                    )
                    .setRunningMode(RunningMode.VIDEO)
                    .setNumPoses(1)
                    .build()
            )

            lastTimestamp = -1L
            recording = true
        } catch (error: Exception) {
            hands?.close()
            hands = null
            pose?.close()
            pose = null
            throw error
        }
    }

    private fun initializeModels() {
        if (interpreter != null) return

        val bytes = appContext.assets
            .open("lensegua_model.tflite")
            .use { it.readBytes() }

        val buffer = ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())

        buffer.put(bytes)
        buffer.rewind()

        val options = Interpreter.Options().setNumThreads(2)
        val loaded = Interpreter(buffer, options)

        try {
            require(
                loaded.getInputTensor(0).shape()
                    .contentEquals(intArrayOf(1, 30, 258))
            ) {
                "El modelo debe recibir [1, 30, 258]."
            }

            require(
                loaded.getOutputTensor(0).shape()
                    .contentEquals(intArrayOf(1, labels.size))
            ) {
                "Las etiquetas no coinciden con las salidas del modelo."
            }
        } catch (error: Exception) {
            loaded.close()
            throw error
        }

        modelBuffer = buffer
        interpreter = loaded
    }

    // Se ejecuta de forma síncrona desde onFrame.
    // No conserva el bitmap que pertenece a la cámara.
    @Synchronized
    fun processFrame(frame: CameraFrame) {
        if (closed || !recording) return

        check(frames.size < 600) {
            "La captura es demasiado larga. Inicia otro intento."
        }

        val rotation = Matrix().apply {
            postRotate(frame.rotationDegrees.toFloat())
        }

        val upright = Bitmap.createBitmap(
            frame.bitmap,
            0,
            0,
            frame.bitmap.width,
            frame.bitmap.height,
            rotation,
            true
        )

        try {
            val image = BitmapImageBuilder(upright).build()

            try {
                val timestamp = maxOf(
                    frame.timestampMillis,
                    lastTimestamp + 1
                )
                lastTimestamp = timestamp

                val handResult = checkNotNull(hands)
                    .detectForVideo(image, timestamp)

                val poseResult = checkNotNull(pose)
                    .detectForVideo(image, timestamp)

                // Los valores ausentes permanecen en cero.
                val values = FloatArray(258)

                // Mismo orden de detección que utiliza el cuaderno.
                handResult.landmarks().take(2)
                    .forEachIndexed { handIndex, landmarks ->
                        landmarks.forEachIndexed { pointIndex, point ->
                            val offset = handIndex * 63 + pointIndex * 3
                            values[offset] = point.x()
                            values[offset + 1] = point.y()
                            values[offset + 2] = point.z()
                        }
                    }

                poseResult.landmarks().firstOrNull()
                    ?.forEachIndexed { pointIndex, point ->
                        val offset = 126 + pointIndex * 4
                        values[offset] = point.x()
                        values[offset + 1] = point.y()
                        values[offset + 2] = point.z()
                        values[offset + 3] =
                            point.visibility().orElse(0f)
                    }

                if (handResult.landmarks().isNotEmpty()) {
                    framesWithHands++
                }

                frames.add(values)
            } finally {
                image.close()
            }
        } finally {
            if (upright !== frame.bitmap && !upright.isRecycled) {
                upright.recycle()
            }
        }
    }

    // Llamar desde un hilo de fondo al terminar la seña.
    @Synchronized
    fun finishCapture(): Prediction {
        check(!closed) { "El reconocedor está cerrado." }
        check(recording) { "Primero inicia una captura." }

        recording = false

        try {
            require(frames.size >= 30) {
                "Faltan imágenes: se capturaron ${frames.size} de 30."
            }
            require(framesWithHands > 0) {
                "No se detectaron manos durante la captura."
            }

            // 30 muestras distribuidas por todo el intento.
            val sequence = Array(30) { index ->
                val sourceIndex =
                    (index.toLong() * (frames.size - 1) / 29).toInt()
                frames[sourceIndex]
            }

            val input = arrayOf(sequence)
            val output = Array(1) { FloatArray(labels.size) }

            checkNotNull(interpreter).run(input, output)

            require(output[0].all { it.isFinite() }) {
                "El modelo devolvió valores inválidos."
            }

            val bestIndex = output[0].indices
                .maxByOrNull { output[0][it] }
                ?: error("El modelo no devolvió resultados.")

            return Prediction(
                label = labels[bestIndex],
                score = output[0][bestIndex]
            )
        } finally {
            frames.clear()
            framesWithHands = 0
        }
    }

    @Synchronized
    fun cancelCapture() {
        recording = false
        frames.clear()
        framesWithHands = 0
    }

    @Synchronized
    override fun close() {
        if (closed) return

        closed = true
        cancelCapture()

        try {
            hands?.close()
        } finally {
            hands = null
            try {
                pose?.close()
            } finally {
                pose = null
                interpreter?.close()
                interpreter = null
                modelBuffer = null
            }
        }
    }
}