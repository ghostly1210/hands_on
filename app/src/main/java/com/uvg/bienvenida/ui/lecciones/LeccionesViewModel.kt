package com.uvg.bienvenida.ui.lecciones

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LeccionesUiState(
    val isLoading: Boolean = false,
    val lecciones: List<String> = emptyList(),
    val errorMessage: String? = null
)

class LeccionesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LeccionesUiState())
    val uiState: StateFlow<LeccionesUiState> = _uiState.asStateFlow()

    init {
        cargarLecciones()
    }

    private fun cargarLecciones() {
    }
}