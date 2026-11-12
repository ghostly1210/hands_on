package com.uvg.bienvenida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.bienvenida.ui.lecciones.*
import com.uvg.bienvenida.ui.theme.HandsonTheme
import com.uvg.bienvenida.ui.camara.CameraScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HandsonTheme { MainAppRoute() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppRoute() {
    var entrada by rememberSaveable { mutableStateOf("bienvenida") }
    if (entrada != "app") {
        BackHandler(enabled = entrada != "bienvenida") { entrada = "bienvenida" }
        EntradaScreen(pantalla = entrada, onNavigate = { entrada = it })
        return
    }
    var tab by rememberSaveable { mutableStateOf("lecciones") }
    var detalle by rememberSaveable { mutableStateOf(false) }
    var videoId by rememberSaveable { mutableStateOf<String?>(null) }
    var camaraAbierta by rememberSaveable { mutableStateOf(false) }
    val video = listaVideosPrueba.firstOrNull { it.id == videoId }
    BackHandler(enabled = true) {
        when {
            camaraAbierta -> camaraAbierta = false
            tab != "lecciones" -> tab = "lecciones"
            videoId != null -> videoId = null
            detalle -> detalle = false
            else -> entrada = "bienvenida"
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == "lecciones", onClick = { camaraAbierta = false; tab = "lecciones" },
                    icon = { Icon(Icons.Default.List, null) }, label = { Text("Lecciones") })
                NavigationBarItem(selected = tab == "reto", onClick = { camaraAbierta = false; tab = "reto" },
                    icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Desafío") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            when {
                camaraAbierta && video != null -> CameraScreen(
                    tituloLeccion = video.titulo,
                    onBack = { camaraAbierta = false }
                )
                tab == "reto" -> RetoDiarioScreen()
                video != null -> Scaffold(topBar = {
                    TopAppBar(title = { Text(video.titulo) }, navigationIcon = {
                        IconButton(onClick = { videoId = null }) { Icon(Icons.Default.ArrowBack, "Volver") }
                    })
                }) { inner ->
                    Column(Modifier.fillMaxSize().padding(inner)
                        .verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (video.videoResId != null) {
                            VideoDelReto(video.videoResId, onReady = {})
                        } else {
                            Text("Video pendiente", style = MaterialTheme.typography.titleLarge)
                            Text("Todavía no se ha agregado el video de esta lección.")
                        }
                        Button(
                            onClick = { camaraAbierta = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Practicar con cámara") }
                    }
                }
                detalle -> VideosScreen(onBackClick = { detalle = false }, onVideoSelect = { videoId = it.id })
                else -> LeccionesRoute(onLeccionClick = { detalle = true })
            }
        }
    }
}
