package com.uvg.bienvenida

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance().apply { setLanguageCode("es") }
    var usuario by mutableStateOf(auth.currentUser)
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    private val listener = FirebaseAuth.AuthStateListener { usuario = it.currentUser }

    init { auth.addAuthStateListener(listener) }

    fun limpiarMensajes() { error = null; mensaje = null }

    fun autenticar(registro: Boolean, nombre: String, correo: String, clave: String, confirmar: String) {
        if (cargando) return
        limpiarMensajes()
        val email = correo.trim()
        when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> error = "Escribe un correo electrónico válido."
            clave.isEmpty() -> error = "Escribe tu contraseña."
            registro && nombre.trim().isEmpty() -> error = "Escribe tu nombre."
            registro && clave.length < 6 -> error = "Usa una contraseña de al menos 6 caracteres."
            registro && clave != confirmar -> error = "Las contraseñas no coinciden."
        }
        if (error != null) return
        cargando = true
        if (registro) {
            auth.createUserWithEmailAndPassword(email, clave).addOnCompleteListener { tarea ->
                if (!tarea.isSuccessful) {
                    error = mensajeError(tarea.exception)
                    cargando = false
                } else {
                    val nuevo = tarea.result?.user
                    if (nuevo == null) {
                        error = "No se pudo recuperar la cuenta. Intenta iniciar sesión."
                        cargando = false
                    } else {
                        val perfil = UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre.trim()).build()
                        nuevo.updateProfile(perfil).addOnCompleteListener { perfilGuardado ->
                            if (!perfilGuardado.isSuccessful) {
                                mensaje = "Tu cuenta se creó, pero no se pudo guardar tu nombre."
                            }
                            usuario = auth.currentUser
                            cargando = false
                        }
                    }
                }
            }
        } else {
            auth.signInWithEmailAndPassword(email, clave).addOnCompleteListener { tarea ->
                if (!tarea.isSuccessful) error = mensajeError(tarea.exception)
                usuario = auth.currentUser
                cargando = false
            }
        }
    }

    fun recuperar(correo: String) {
        if (cargando) return
        limpiarMensajes()
        val email = correo.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error = "Escribe primero un correo electrónico válido."
            return
        }
        cargando = true
        auth.sendPasswordResetEmail(email).addOnCompleteListener { tarea ->
            cargando = false
            // No revelar si un correo tiene cuenta o no.
            if (tarea.isSuccessful || (tarea.exception as? FirebaseAuthException)?.errorCode == "ERROR_USER_NOT_FOUND") {
                mensaje = "Si existe una cuenta con ese correo, recibirás un enlace para cambiar tu contraseña. Revisa también spam."
            } else error = mensajeError(tarea.exception)
        }
    }

    fun cerrarSesion() {
        if (cargando) return
        auth.signOut()
        usuario = null
        limpiarMensajes()
    }

    override fun onCleared() {
        auth.removeAuthStateListener(listener)
        super.onCleared()
    }
}

private fun mensajeError(e: Exception?): String = when (e) {
    is FirebaseNetworkException -> "No hay conexión con Firebase. Revisa tu internet e intenta de nuevo."
    is FirebaseTooManyRequestsException -> "Hubo demasiados intentos. Espera un momento e intenta de nuevo."
    is FirebaseAuthWeakPasswordException -> "La contraseña no cumple los requisitos: ${e.reason}"
    is FirebaseAuthException -> when (e.errorCode) {
        "ERROR_INVALID_EMAIL" -> "El correo electrónico no tiene un formato válido."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "No se pudo crear la cuenta. Prueba iniciar sesión o recuperar tu contraseña."
        "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada. Contacta al equipo de HandsOn."
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_LOGIN_CREDENTIALS" ->
            "No se pudo iniciar sesión. Revisa tu correo y contraseña."
        "ERROR_OPERATION_NOT_ALLOWED" -> "El acceso con correo y contraseña no está habilitado en Firebase."
        else -> "No se pudo completar la operación (${e.errorCode}). Intenta de nuevo."
    }
    else -> "No se pudo completar la operación. Revisa tu conexión e intenta de nuevo."
}
