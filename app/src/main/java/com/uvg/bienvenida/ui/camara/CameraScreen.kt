package com.uvg.bienvenida.ui.camara

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    tituloLeccion: String,
    onBack: () -> Unit,
    onFrame: ((CameraFrame) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recognition = remember(context, tituloLeccion) { CameraRecognitionSession(context.applicationContext) }
    val internalRecognition = onFrame == null
    val recognitionState = recognition.state
    DisposableEffect(recognition) {
        onDispose { recognition.close() }
    }
    LaunchedEffect(internalRecognition) { recognition.cancel() }
    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    var granted by remember { mutableStateOf(hasPermission()) }
    var denied by rememberSaveable { mutableStateOf(false) }
    var visible by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    var requestedLens by rememberSaveable { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var actualLens by remember { mutableIntStateOf(requestedLens) }
    var canSwitch by remember { mutableStateOf(false) }
    var streaming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        denied = !it
        error = null
    }

    DisposableEffect(lifecycleOwner, recognition) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> visible = true
                Lifecycle.Event.ON_RESUME -> {
                    // Actualiza también al regresar de Configuración o de un permiso temporal.
                    granted = hasPermission()
                    error = null
                }
                Lifecycle.Event.ON_STOP -> { visible = false; streaming = false; recognition.cancel() }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Practicar seña") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver a la lección") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(tituloLeccion, style = MaterialTheme.typography.titleLarge)
            Text("Coloca tus manos dentro de la imagen, con buena iluminación.")
            when {
                !granted -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (denied) "El permiso de cámara no está habilitado." else "Activa la cámara para practicar.")
                            Text("La cámara permite ver tus señas mientras practicas.")
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Permitir cámara")
                            }
                            if (denied) {
                                Text("Si Android ya no muestra la solicitud, habilita Cámara en los permisos de la app.")
                                TextButton(onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:${context.packageName}")))
                                    } catch (_: android.content.ActivityNotFoundException) {
                                        error = "Abre Configuración del teléfono y habilita el permiso de Cámara para HandsOn."
                                    }
                                }) { Text("Abrir configuración") }
                                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
                error != null -> {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        granted = hasPermission()
                        streaming = false
                        canSwitch = false
                        error = null
                        attempt++
                    }) { Text("Reintentar cámara") }
                }
                visible -> {
                    key(attempt, requestedLens) {
                        CameraPreview(
                            requestedLens = requestedLens,
                            modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(20.dp)).background(Color.Black),
                            onFrame = onFrame ?: recognition::onFrame,
                            onCameraInfo = { lens, switch -> actualLens = lens; canSwitch = switch },
                            onStreaming = { streaming = it },
                            onError = { streaming = false; error = it; recognition.cancel() }
                        )
                    }
                    Text(if (streaming) "Cámara lista" else "Abriendo cámara…")
                    Text(if (actualLens == CameraSelector.LENS_FACING_FRONT)
                        "Cámara frontal · vista en espejo" else "Cámara trasera")
                    OutlinedButton(enabled = canSwitch && !recognitionState.busy, onClick = {
                        recognition.cancel()
                        streaming = false
                        canSwitch = false
                        requestedLens = if (actualLens == CameraSelector.LENS_FACING_FRONT)
                            CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                    }) { Text("Cambiar cámara") }
                }
                else -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("Cámara pausada")
                }
            }
            if (internalRecognition) {
                Text(recognitionState.message, style = MaterialTheme.typography.bodyMedium)
                if (recognitionState.capturing) {
                    Text("Imágenes procesadas: ${recognitionState.count} (mínimo 30)")
                    Text("Realiza la seña completa y después toca Evaluar seña.")
                    Button(
                        enabled = recognitionState.count >= 30,
                        onClick = { recognition.finish() }
                    ) { Text("Evaluar seña") }
                } else {
                    Button(
                        enabled = granted && visible && streaming && error == null && !recognitionState.busy,
                        onClick = { recognition.start() }
                    ) { Text("Iniciar intento") }
                }
                if (recognitionState.busy) {
                    TextButton(onClick = { recognition.cancel() }) { Text("Cancelar intento") }
                }
                recognitionState.prediction?.let { prediction ->
                    Text("Seña detectada: ${prediction.label}", style = MaterialTheme.typography.titleMedium)
                    Text("Puntuación del modelo: ${String.format(java.util.Locale.US, "%.3f", prediction.score)}")
                    Text("Esta predicción todavía no confirma que la seña de la lección sea correcta.")
                }
            } else {
                Text("Análisis gestionado por la pantalla que abrió la cámara.")
            }
            Text("Las imágenes se usan en memoria: esta pantalla no guarda fotos ni graba videos.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}


private data class CameraRecognitionState(
    val message: String = "Pulsa Iniciar intento y espera a que indique que puedes hacer la seña.",
    val busy: Boolean = false,
    val capturing: Boolean = false,
    val count: Int = 0,
    val prediction: SignLanguageRecognizer.Prediction? = null
)

/** Un solo hilo para cargar, analizar y cerrar los modelos. No retiene bitmaps de CameraX. */
private class CameraRecognitionSession(private val context: Context) {
    var state by mutableStateOf(CameraRecognitionState())
        private set
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val disposed = AtomicBoolean(false)
    private val accepting = AtomicBoolean(false)
    private val framePending = AtomicBoolean(false)
    private val generation = AtomicInteger(0)
    // Solo se accede a estos dos campos en executor.
    private var recognizer: SignLanguageRecognizer? = null
    private var count = 0

    private fun current(token: Int) = !disposed.get() && generation.get() == token

    private fun publish(token: Int, value: CameraRecognitionState) {
        main.post {
            if (current(token) && (!value.capturing || accepting.get())) state = value
        }
    }

    fun start() {
        if (disposed.get() || state.busy) return
        accepting.set(false)
        val token = generation.incrementAndGet()
        state = CameraRecognitionState("Preparando reconocimiento…", busy = true)
        executor.execute {
            if (current(token)) {
                try {
                    val model = recognizer ?: SignLanguageRecognizer(context).also { recognizer = it }
                    model.startCapture()
                    count = 0
                    if (current(token)) {
                        accepting.set(true)
                        publish(token, CameraRecognitionState("Haz la seña ahora.", true, true))
                    } else model.cancelCapture()
                } catch (error: Exception) { fail(token, error) }
            }
        }
    }

    fun onFrame(frame: CameraFrame) {
        val token = generation.get()
        if (!current(token) || !accepting.get() || !framePending.compareAndSet(false, true)) return
        // CameraPreview recicla el original al regresar. Copiar antes de enviar al hilo del modelo.
        val copy = try {
            checkNotNull(frame.bitmap.copy(Bitmap.Config.ARGB_8888, false))
        } catch (error: Exception) {
            framePending.set(false)
            accepting.set(false)
            publish(token, CameraRecognitionState("No se pudo copiar la imagen: ${error.message}"))
            return
        }
        try {
            executor.execute {
                try {
                    if (current(token) && accepting.get() && count < 600) {
                        checkNotNull(recognizer).processFrame(frame.copy(bitmap = copy))
                        count++
                        publish(token, CameraRecognitionState(
                            if (count >= 600) "Límite de imágenes alcanzado. Toca Evaluar seña." else "Haz la seña ahora.",
                            true, true, count
                        ))
                    }
                } catch (error: Exception) { fail(token, error) }
                finally { copy.recycle(); framePending.set(false) }
            }
        } catch (_: RejectedExecutionException) {
            copy.recycle()
            framePending.set(false)
        }
    }

    fun finish() {
        if (disposed.get() || !state.capturing || state.count < 30) return
        accepting.set(false)
        val token = generation.get()
        state = CameraRecognitionState("Evaluando seña…", busy = true)
        executor.execute { if (current(token)) evaluate(token) }
    }

    private fun evaluate(token: Int) {
        try {
            val result = checkNotNull(recognizer).finishCapture()
            publish(token, CameraRecognitionState("Intento terminado. Puedes volver a intentarlo.", prediction = result))
        } catch (error: Exception) { fail(token, error) }
    }

    private fun fail(token: Int, error: Exception) {
        if (current(token)) accepting.set(false)
        recognizer?.cancelCapture()
        publish(token, CameraRecognitionState("No se pudo evaluar: ${error.message ?: error.javaClass.simpleName}"))
    }

    fun cancel() {
        if (disposed.get()) return
        generation.incrementAndGet()
        accepting.set(false)
        state = CameraRecognitionState()
        executor.execute { recognizer?.cancelCapture() }
    }

    fun close() {
        if (!disposed.compareAndSet(false, true)) return
        generation.incrementAndGet()
        accepting.set(false)
        executor.execute {
            try { recognizer?.close() }
            finally { recognizer = null }
        }
        executor.shutdown()
    }
}
