package com.uvg.bienvenida

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * INSTALACIÓN
 * 1. Copia este archivo al mismo paquete que MainActivity.kt.
 * 2. Dentro de HandsonTheme, cambia LessonsScreen() por RetoDiarioScreen().
 * 3. Copia TU video a app/src/main/res/raw/reto_diario.mp4.
 * 4. Cambia abajo videoResId: Int? = null por:
 *       videoResId: Int? = R.raw.reto_diario
 *    Primero agrega el archivo de video para que R.raw.reto_diario exista.
 * 5. Edita opciones e indiceCorrecto para que coincidan con tu video.
 *
 * No requiere modificar Gradle ni permisos de cámara o internet.
 * Prototipo local: un intento al día según la fecha del teléfono.
 * Los puntos se guardan en este dispositivo, sin cuentas ni servidor.
 * El mismo video se repite cada día hasta que cambies la configuración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetoDiarioScreen(
    videoResId: Int? = R.raw.reto_diario, // TU VIDEO: reemplaza null por R.raw.reto_diario
    opciones: List<String> = listOf("¿Cuál es tu nombre?", "Gracias", "Por favor", "Adiós"),
    indiceCorrecto: Int = 0, // 0 = primera, 1 = segunda, 2 = tercera, 3 = cuarta
    puntosPorAcierto: Int = 10
) {
    require(opciones.size == 4) { "El reto necesita cuatro opciones" }
    require(indiceCorrecto in opciones.indices)
    require(puntosPorAcierto > 0)

    val context = LocalContext.current
    val preferencias = remember(context) {
        context.getSharedPreferences("handson_reto_diario", Context.MODE_PRIVATE)
    }
    val lifecycle = LocalView.current.findViewTreeLifecycleOwner()?.lifecycle
    var fecha by remember { mutableStateOf(LocalDate.now()) }
    // Actualiza la fecha al volver a la app y también si queda abierta a medianoche.
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) fecha = LocalDate.now()
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            fecha = LocalDate.now()
            delay(30_000)
        }
    }

    val claveDia = fecha.toString()
    var seleccion by remember(claveDia) {
        mutableStateOf(preferencias.getInt("respuesta_$claveDia", -1))
    }
    var resultado by remember(claveDia) {
        mutableStateOf(preferencias.getString("resultado_$claveDia", "") ?: "")
    }
    var total by remember(claveDia) {
        mutableStateOf(preferencias.getInt("puntos_totales", 0))
    }
    var iniciado by rememberSaveable(claveDia) { mutableStateOf(false) }
    var videoListo by remember(claveDia, videoResId) { mutableStateOf(false) }
    val respondido = seleccion >= 0
    val azul = MaterialTheme.colorScheme.primary
    val fechaVisible = fecha.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "GT")))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reto diario", fontWeight = FontWeight.Bold) },
                actions = {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(20.dp)) {
                        Text("$total puntos", Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = azul, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(fechaVisible.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge)
                    Text("¿Reconoces esta seña?", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text("Observa el video y elige su significado.")
                    Text("+$puntosPorAcierto puntos al acertar", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            if (!iniciado && !respondido) {
                Spacer(Modifier.height(24.dp))
                Text("¿Listo para el desafío?", style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center)
                Text("Practica lo que aprendiste. Tendrás cuatro opciones y un intento para responder.",
                    textAlign = TextAlign.Center)
                Button(
                    onClick = { iniciado = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = azul)
                ) { Text("Empezar desafío") }
            } else {
                key(claveDia, videoResId) {
                    if (videoResId == null) {
                        Surface(shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("Aquí va el video.\nAgregar reto_diario.mp4 a res/raw y configurar videoResId.",
                                Modifier.fillMaxWidth().padding(28.dp), textAlign = TextAlign.Center)
                        }
                    } else {
                        VideoDelReto(videoResId, onReady = { videoListo = it })
                    }
                }
                Text("¿Qué significa esta seña?", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                opciones.forEachIndexed { index, opcion ->
                    val esCorrecta = respondido && index == indiceCorrecto
                    val esIncorrecta = respondido && index == seleccion && index != indiceCorrecto
                    val fondo = when {
                        esCorrecta -> MaterialTheme.colorScheme.tertiaryContainer
                        esIncorrecta -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val texto = when {
                        esCorrecta -> MaterialTheme.colorScheme.onTertiaryContainer
                        esIncorrecta -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    OutlinedButton(
                        enabled = !respondido && videoListo,
                        onClick = {
                            // Consulta el almacenamiento también para impedir premios duplicados.
                            if (LocalDate.now() != fecha) {
                                fecha = LocalDate.now()
                            } else if (!preferencias.contains("respuesta_$claveDia")) {
                                val acierto = index == indiceCorrecto
                                val nuevoTotal = preferencias.getInt("puntos_totales", 0) +
                                        if (acierto) puntosPorAcierto else 0
                                val mensaje = if (acierto) "¡Correcto! Acumulas $puntosPorAcierto puntos."
                                else "La respuesta correcta era: ${opciones[indiceCorrecto]}."
                                preferencias.edit()
                                    .putInt("respuesta_$claveDia", index)
                                    .putString("resultado_$claveDia", mensaje)
                                    .putInt("puntos_totales", nuevoTotal).apply()
                                seleccion = index
                                resultado = mensaje
                                total = nuevoTotal
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = fondo, contentColor = texto,
                            disabledContainerColor = fondo,
                            disabledContentColor = if (respondido) texto else texto.copy(alpha = 0.38f)
                        )
                    ) {
                        val etiqueta = when {
                            esCorrecta -> "Correcta: "
                            esIncorrecta -> "Tu respuesta: "
                            else -> "${('A'.code + index).toChar()}. "
                        }
                        Text(etiqueta + opcion, Modifier.fillMaxWidth())
                    }
                }
                if (respondido) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(resultado, fontWeight = FontWeight.Bold)
                            Text("Reto de hoy completado. Vuelve mañana para intentarlo de nuevo.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun VideoDelReto(videoResId: Int, onReady: (Boolean) -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalView.current.findViewTreeLifecycleOwner()?.lifecycle
    val readyCallback by rememberUpdatedState(onReady)
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var preparado by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var intento by remember { mutableStateOf(0) }
    var posicion by rememberSaveable(videoResId) { mutableStateOf(0) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                posicion = videoView?.currentPosition ?: posicion
                videoView?.pause()
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            posicion = videoView?.currentPosition ?: posicion
            videoView?.stopPlayback()
            lifecycle?.removeObserver(observer)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        key(intento) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(260.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Color.Black),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoView = this
                        setOnPreparedListener { player ->
                            player.isLooping = false
                            preparado = true
                            error = false
                            readyCallback(true)
                            seekTo(posicion)
                            if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) start()
                        }
                        setOnErrorListener { _, _, _ ->
                            error = true
                            preparado = false
                            readyCallback(false)
                            true
                        }
                        setVideoURI(Uri.parse("android.resource://${context.packageName}/$videoResId"))
                    }
                },
                onReset = null,
                onRelease = { it.stopPlayback() }
            )
        }
        if (error) {
            Text("No se pudo reproducir el video. Revisa el archivo; puedes probar MP4 con video H.264.",
                color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { error = false; posicion = 0; intento++ }) { Text("Reintentar video") }
        } else {
            if (!preparado) Text("Cargando video…")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(modifier = Modifier.weight(1f), enabled = preparado, onClick = { videoView?.start() }) { Text("Reproducir") }
                TextButton(modifier = Modifier.weight(1f), enabled = preparado, onClick = { videoView?.pause() }) { Text("Pausar") }
                TextButton(modifier = Modifier.weight(1f), enabled = preparado, onClick = {
                    posicion = 0
                    videoView?.seekTo(0)
                    videoView?.start()
                }) { Text("Repetir") }
            }
        }
    }
}
