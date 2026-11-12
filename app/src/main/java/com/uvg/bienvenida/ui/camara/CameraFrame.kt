package com.uvg.bienvenida.ui.camara

import android.graphics.Bitmap

/**
 * Contrato de entrega para el futuro analizador de señas.
 *
 * [bitmap] contiene todos los píxeles, sin girar ni reflejar. [rotationDegrees]
 * indica el giro horario necesario para orientarlos. La vista frontal se ve
 * como espejo, pero los píxeles entregados al analizador NO están reflejados.
 * [timestampMillis] es monotónico desde el arranque, no una fecha de calendario.
 *
 * El callback se ejecuta de forma SÍNCRONA en un hilo de cámara. El bitmap se
 * recicla al regresar: no guardarlo ni pasarlo directamente a detectAsync.
 * Si el modelo necesita conservarlo, hacer una copia y liberar esa copia
 * al terminar. No actualizar Views desde este hilo.
 */
data class CameraFrame(
    val bitmap: Bitmap,
    val rotationDegrees: Int,
    val timestampMillis: Long,
    val isFrontCamera: Boolean
)
