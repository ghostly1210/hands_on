package com.uvg.bienvenida

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.tooling.preview.Preview
import com.uvg.bienvenida.ui.theme.HandsonTheme

@Composable
fun EntradaScreen(pantalla: String, onNavigate: (String) -> Unit) {
    var aviso by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            val alto = maxHeight
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).heightIn(min = alto).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pantalla != "bienvenida") {
                        TextButton(onClick = { onNavigate("bienvenida") }, modifier = Modifier.align(Alignment.Start)) {
                            Text("Volver")
                        }
                    }
                    Text("HandsOn", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (pantalla == "bienvenida") {
                        Text("Tu puente hacia la comunidad sorda de Guatemala.",
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Image(painterResource(R.drawable.logo_quetzal), "Quetzal, mascota de HandsOn",
                            Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 280.dp).fillMaxWidth().aspectRatio(1f))
                        Button(onClick = { onNavigate("login") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary)) { Text("Empezar") }
                        Button(onClick = { onNavigate("app") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(12.dp)) {
                            Text("¡Echa un vistazo!")
                        }
                        TextButton(onClick = { onNavigate("registro") }) { Text("¿No tienes cuenta? Regístrate") }
                    } else {
                        val registro = pantalla == "registro"
                        var correo by rememberSaveable(pantalla) { mutableStateOf("") }
                        // La contraseña se conserva solo mientras este formulario está en memoria.
                        var clave by remember(pantalla) { mutableStateOf("") }
                        var visible by remember { mutableStateOf(false) }
                        Card(shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(if (registro) "Crea tu cuenta" else "Bienvenido de nuevo",
                                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("¡Continúa tu viaje de aprendizaje!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedTextField(value = correo, onValueChange = { correo = it },
                                    label = { Text("Correo electrónico") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = clave, onValueChange = { clave = it },
                                    label = { Text("Contraseña") }, singleLine = true,
                                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") } },
                                    modifier = Modifier.fillMaxWidth())
                                if (!registro) TextButton(onClick = { aviso = true }) { Text("¿Olvidaste tu contraseña?") }
                                Text("El acceso con cuenta todavía no está disponible. Puedes explorar como invitado.",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(onClick = { aviso = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                    Text(if (registro) "Crear cuenta" else "Iniciar sesión")
                                }
                            }
                        }
                        TextButton(onClick = { onNavigate(if (registro) "login" else "registro") }) {
                            Text(if (registro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Crea una cuenta")
                        }
                        OutlinedButton(onClick = { onNavigate("app") }, modifier = Modifier.fillMaxWidth()) {
                            Text("Continuar como invitado")
                        }
                    }
                }
            }
        }
    }
    if (aviso) AlertDialog(onDismissRequest = { aviso = false },
        title = { Text("Cuentas próximamente") },
        text = { Text("El registro, el inicio de sesión y la recuperación de contraseña aún no están habilitados. No se envió ni guardó tu contraseña.") },
        confirmButton = { TextButton(onClick = { aviso = false; onNavigate("app") }) { Text("Entrar como invitado") } },
        dismissButton = { TextButton(onClick = { aviso = false }) { Text("Volver") } })
}

@Preview(showBackground = true)
@Composable
private fun BienvenidaClara() { HandsonTheme(darkTheme = false) { EntradaScreen("bienvenida", {}) } }
@Preview(showBackground = true)
@Composable
private fun BienvenidaOscura() { HandsonTheme(darkTheme = true) { EntradaScreen("bienvenida", {}) } }
@Preview(showBackground = true)
@Composable
private fun LoginOscuro() { HandsonTheme(darkTheme = true) { EntradaScreen("login", {}) } }