package com.uvg.bienvenida.ui.camara

import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** Solo se monta cuando hay permiso y la pantalla está visible. */
@Composable
internal fun CameraPreview(
    requestedLens: Int,
    modifier: Modifier = Modifier,
    onFrame: ((CameraFrame) -> Unit)?,
    onCameraInfo: (actualLens: Int, canSwitch: Boolean) -> Unit,
    onStreaming: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val frameCallback = rememberUpdatedState(onFrame)
    val infoCallback = rememberUpdatedState(onCameraInfo)
    val streamingCallback = rememberUpdatedState(onStreaming)
    val errorCallback = rememberUpdatedState(onError)
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            // Muestra la imagen completa; evita recortar dedos en el encuadre.
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(previewView, lifecycleOwner, requestedLens) {
        val active = AtomicBoolean(true)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val executor = Executors.newSingleThreadExecutor()
        val resolution = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(
                ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
            ).build()
        val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
        val preview = Preview.Builder().setTargetRotation(rotation).build()
        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolution)
            .setTargetRotation(rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        var provider: ProcessCameraProvider? = null
        var cameraState: LiveData<CameraState>? = null
        val cameraObserver = Observer<CameraState> { state ->
            if (active.get() && state.error != null) {
                errorCallback.value("No se puede usar la cámara. Cierra otras apps que la estén usando y reintenta.")
            }
        }
        val streamObserver = Observer<PreviewView.StreamState> { state ->
            if (active.get()) streamingCallback.value(state == PreviewView.StreamState.STREAMING)
        }
        previewView.previewStreamState.observe(lifecycleOwner, streamObserver)

        // También cubre giros de 180° que no recrean la actividad.
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) {
                val display = previewView.display ?: return
                if (active.get() && display.displayId == displayId) {
                    preview.targetRotation = display.rotation
                    analysis.targetRotation = display.rotation
                }
            }
        }
        displayManager.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (active.get()) {
                try {
                    val currentProvider = future.get()
                    provider = currentProvider
                    val front = currentProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    val back = currentProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    check(front || back) { "No hay cámaras disponibles" }
                    val actualLens = when {
                        requestedLens == CameraSelector.LENS_FACING_FRONT && front -> CameraSelector.LENS_FACING_FRONT
                        requestedLens == CameraSelector.LENS_FACING_BACK && back -> CameraSelector.LENS_FACING_BACK
                        front -> CameraSelector.LENS_FACING_FRONT
                        else -> CameraSelector.LENS_FACING_BACK
                    }
                    val selector = CameraSelector.Builder().requireLensFacing(actualLens).build()
                    infoCallback.value(actualLens, front && back)
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    var lastFrameTime = -1L
                    analysis.setAnalyzer(executor) { image ->
                        try {
                            val callback = frameCallback.value
                            val now = SystemClock.elapsedRealtime()
                            // Hasta 10 entregas/s; si el modelo tarda más, CameraX descarta imágenes viejas.
                            if (active.get() && callback != null &&
                                (lastFrameTime < 0 || now - lastFrameTime >= 100)
                            ) {
                                lastFrameTime = now
                                val bitmap = image.toBitmap()
                                try {
                                    callback(CameraFrame(bitmap, image.imageInfo.rotationDegrees,
                                        now, actualLens == CameraSelector.LENS_FACING_FRONT))
                                } finally {
                                    bitmap.recycle()
                                }
                            }
                        } catch (error: Exception) {
                            Log.e("HandsOnCamera", "Error en el consumidor de imágenes", error)
                            mainExecutor.execute {
                                if (active.get()) errorCallback.value("No se pudo procesar la imagen. Reintenta la cámara.")
                            }
                        } finally {
                            // Indispensable incluso si falla el futuro analizador.
                            image.close()
                        }
                    }
                    val camera = currentProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                    cameraState = camera.cameraInfo.cameraState
                    cameraState?.observe(lifecycleOwner, cameraObserver)
                } catch (error: Exception) {
                    Log.e("HandsOnCamera", "No se pudo iniciar la cámara", error)
                    if (active.get()) errorCallback.value(
                        "No se pudo abrir la cámara. Comprueba el permiso y que tu dispositivo tenga una cámara disponible."
                    )
                }
            }
        }, mainExecutor)

        onDispose {
            // El proveedor es compartido: no cancelar su Future ni usar unbindAll().
            active.set(false)
            displayManager.unregisterDisplayListener(displayListener)
            previewView.previewStreamState.removeObserver(streamObserver)
            cameraState?.removeObserver(cameraObserver)
            analysis.clearAnalyzer()
            provider?.unbind(preview, analysis)
            preview.setSurfaceProvider(null)
            executor.shutdown()
        }
    }
}
