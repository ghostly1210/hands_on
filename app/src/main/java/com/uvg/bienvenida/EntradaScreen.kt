package com.uvg.bienvenida

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EntradaScreen(
    pantalla: String,
    onNavigate: (String) -> Unit,
    auth: AuthViewModel = viewModel()
) {
    val cargando = auth.cargando
    // Evita abandonar el formulario mientras Firebase está procesando la petición.
    BackHandler(enabled = cargando) { }
    val navegar: (String) -> Unit = { destino ->
        if (!cargando) { auth.limpiarMensajes(); onNavigate(destino) }
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            val alto = maxHeight
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .heightIn(min = alto).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (pantalla != "bienvenida") {
                        TextButton(onClick = { navegar("bienvenida") }, enabled = !cargando,
                            modifier = Modifier.align(Alignment.Start)) { Text("Volver") }
                    }
                    Text("HandsOn", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (pantalla == "bienvenida") {
                        Text("Tu puente hacia la comunidad sorda de Guatemala.", textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Image(painterResource(R.drawable.logo_quetzal), "Quetzal, mascota de HandsOn",
                            Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 280.dp).fillMaxWidth().aspectRatio(1f))
                        Button(onClick = { navegar("login") }, enabled = !cargando,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary)) { Text("Empezar") }
                        Button(onClick = { navegar("app") }, enabled = !cargando,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp)) { Text("¡Echa un vistazo!") }
                        TextButton(onClick = { navegar("registro") }, enabled = !cargando) {
                            Text("¿No tienes cuenta? Regístrate")
                        }
                    } else {
                        val registro = pantalla == "registro"
                        val recuperacion = pantalla == "recuperar"
                        var nombre by rememberSaveable(pantalla) { mutableStateOf("") }
                        var correo by rememberSaveable(pantalla) { mutableStateOf("") }
                        // Contraseñas solo en memoria, nunca en rememberSaveable ni archivos.
                        var clave by remember(pantalla) { mutableStateOf("") }
                        var confirmar by remember(pantalla) { mutableStateOf("") }
                        var visible by remember(pantalla) { mutableStateOf(false) }
                        Card(shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(when {
                                    registro -> "Crea tu cuenta"
                                    recuperacion -> "Recupera tu contraseña"
                                    else -> "Bienvenido de nuevo"
                                }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(if (recuperacion) "Te enviaremos un enlace a tu correo."
                                    else "¡Continúa tu viaje de aprendizaje!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (registro) OutlinedTextField(
                                    value = nombre, onValueChange = { nombre = it }, enabled = !cargando,
                                    label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(
                                    value = correo, onValueChange = { correo = it }, enabled = !cargando,
                                    label = { Text("Correo electrónico") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth())
                                if (!recuperacion) {
                                    OutlinedTextField(
                                        value = clave, onValueChange = { clave = it }, enabled = !cargando,
                                        label = { Text("Contraseña") }, singleLine = true,
                                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        trailingIcon = {
                                            TextButton(onClick = { visible = !visible }, enabled = !cargando) {
                                                Text(if (visible) "Ocultar" else "Ver")
                                            }
                                        }, modifier = Modifier.fillMaxWidth())
                                    if (registro) OutlinedTextField(
                                        value = confirmar, onValueChange = { confirmar = it }, enabled = !cargando,
                                        label = { Text("Confirmar contraseña") }, singleLine = true,
                                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth())
                                }
                                auth.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                auth.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                                if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                                Button(
                                    enabled = !cargando,
                                    onClick = {
                                        if (recuperacion) auth.recuperar(correo)
                                        else auth.autenticar(registro, nombre, correo, clave, confirmar)
                                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) { Text(when {
                                    cargando -> "Espera un momento…"
                                    recuperacion -> "Enviar enlace"
                                    registro -> "Crear cuenta"
                                    else -> "Iniciar sesión"
                                }) }
                                if (!registro && !recuperacion) TextButton(
                                    onClick = { navegar("recuperar") }, enabled = !cargando
                                ) { Text("¿Olvidaste tu contraseña?") }
                            }
                        }
                        TextButton(onClick = { navegar(if (registro || recuperacion) "login" else "registro") },
                            enabled = !cargando) {
                            Text(if (registro || recuperacion) "Volver a iniciar sesión" else "¿No tienes cuenta? Crea una cuenta")
                        }
                        OutlinedButton(onClick = { navegar("app") }, enabled = !cargando,
                            modifier = Modifier.fillMaxWidth()) { Text("Continuar como invitado") }
                    }
                }
            }
        }
    }
}
