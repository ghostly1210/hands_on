package com.uvg.bienvenida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun MainAppRoute(auth: AuthViewModel = viewModel()) {
    var entrada by rememberSaveable { mutableStateOf("bienvenida") }
    var invitado by rememberSaveable { mutableStateOf(false) }
    val usuario = auth.usuario

    // Firebase restaura automáticamente la sesión local al abrir la aplicación.
    if ((usuario == null && !invitado) || auth.cargando) {
        BackHandler(enabled = entrada != "bienvenida" && !auth.cargando) {
            auth.limpiarMensajes()
            entrada = "bienvenida"
        }
        EntradaScreen(pantalla = entrada, auth = auth, onNavigate = { destino ->
            if (destino == "app") invitado = true else entrada = destino
        })
        return
    }

    // Al cambiar de usuario se descarta la navegación de la sesión anterior.
    key(usuario?.uid ?: "invitado") {
        ContenidoHandsOn(auth = auth, onSalir = {
            invitado = false
            entrada = "bienvenida"
            auth.cerrarSesion()
        }, onIniciarSesion = {
            invitado = false
            entrada = "login"
            auth.limpiarMensajes()
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoHandsOn(auth: AuthViewModel, onSalir: () -> Unit, onIniciarSesion: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf("lecciones") }
    var detalle by rememberSaveable { mutableStateOf(false) }
    var videoId by rememberSaveable { mutableStateOf<String?>(null) }
    var camaraAbierta by rememberSaveable { mutableStateOf(false) }
    val video = listaVideosPrueba.firstOrNull { it.id == videoId }

    // En la raíz, Atrás queda en manos de Android: no cierra la sesión.
    BackHandler(enabled = camaraAbierta || tab != "lecciones" || videoId != null || detalle) {
        when {
            camaraAbierta -> camaraAbierta = false
            tab != "lecciones" -> tab = "lecciones"
            videoId != null -> videoId = null
            detalle -> detalle = false
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
                NavigationBarItem(selected = tab == "cuenta", onClick = { camaraAbierta = false; tab = "cuenta" },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Cuenta") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            when {
                camaraAbierta && video != null -> CameraScreen(
                    tituloLeccion = video.titulo, onBack = { camaraAbierta = false })
                tab == "reto" -> RetoDiarioScreen()
                tab == "cuenta" -> CuentaScreen(auth, onSalir, onIniciarSesion)
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
                        Button(onClick = { camaraAbierta = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Practicar con cámara")
                        }
                    }
                }
                detalle -> VideosScreen(onBackClick = { detalle = false }, onVideoSelect = { videoId = it.id })
                else -> LeccionesRoute(onLeccionClick = { detalle = true })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuentaScreen(auth: AuthViewModel, onSalir: () -> Unit, onIniciarSesion: () -> Unit) {
    val usuario = auth.usuario
    Scaffold(topBar = { TopAppBar(title = { Text("Mi cuenta") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (usuario == null) {
                Text("Estás explorando como invitado", style = MaterialTheme.typography.titleLarge)
                Button(onClick = onIniciarSesion, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión o crear cuenta") }
                OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) { Text("Volver a la bienvenida") }
            } else {
                Text(usuario.displayName?.takeIf { it.isNotBlank() } ?: "Estudiante de HandsOn",
                    style = MaterialTheme.typography.headlineSmall)
                Text(usuario.email.orEmpty())
                auth.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                Button(onClick = onSalir, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
            }
        }
    }
}
