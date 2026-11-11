# Cámara de HandsOn

La integración está hecha sobre el ZIP que compartiste. Es una implementación
de cámara en vivo para Android con Kotlin, Jetpack Compose y CameraX 1.6.2.
No incluye reconocimiento de manos, validación de señas ni premios por practicar.

## Abrir y probar

1. Descomprime el ZIP en una carpeta nueva y abre `hands_on-main` con Android Studio.
2. Deja que Gradle sincronice las dependencias. Conservamos la configuración
   original: compileSdk/targetSdk 37, AGP 9.4.1, Gradle 9.6.1 y el daemon JDK 21.
   Android Studio debe tener los componentes que ese proyecto solicita.
3. Ejecuta la app en un teléfono Android (Android 8/API 26 o posterior).
   Un emulador necesita tener una cámara configurada; es preferible un teléfono.
4. Entra como invitado o usa el flujo de acceso existente.
5. Abre **Lecciones → Nivel Básico → una palabra → Practicar con cámara**.
6. Pulsa **Permitir cámara** y acepta la solicitud del sistema. Debe aparecer
   la vista frontal y el texto **Cámara lista**. Si solo hay cámara trasera,
   se usa esa automáticamente. **Cambiar cámara** se habilita cuando hay ambas.

Esta pantalla no graba videos, no guarda fotos, no usa el micrófono y no envía
imágenes a un servidor. Solo declara el permiso CAMERA. Las imágenes de análisis
se preparan en memoria cuando se conecta un consumidor `onFrame`.

## Archivos de tu parte

| Archivo | Cambio |
| --- | --- |
| `gradle/libs.versions.toml` | Versiones y alias de CameraX y Lifecycle Compose. |
| `app/build.gradle.kts` | Dependencias para cámara, vista previa y ciclo de vida. |
| `app/src/main/AndroidManifest.xml` | Permiso CAMERA; la cámara es opcional para instalar la app. |
| `app/src/main/java/com/uvg/bienvenida/MainActivity.kt` | Botón, navegación hacia la cámara y regreso a la lección. |
| `app/src/main/java/com/uvg/bienvenida/ui/camara/CameraScreen.kt` | Interfaz, permisos, configuración, cambio de cámara y reintento. |
| `app/src/main/java/com/uvg/bienvenida/ui/camara/CameraPreview.kt` | Preview, sesión de CameraX, orientación y entrega de imágenes. |
| `app/src/main/java/com/uvg/bienvenida/ui/camara/CameraFrame.kt` | Contrato de datos para tu compañera. |

La sesión se vincula al ciclo de vida. Al salir de la pantalla, cambiar de
pestaña o mandar la app al fondo, se retiran sus casos de uso y su analizador.
Una inicialización que termine después de salir no puede volver a abrirla.
Al regresar del fondo se revisa el permiso de nuevo.

El video de la lección sale de la composición al abrir la cámara, por lo que
su reproductor existente se detiene. El botón y la nueva pantalla permiten
desplazamiento para acceder a los controles en pantallas pequeñas u horizontales.

## Conexión para tu compañera

El lugar de conexión está en la llamada a `CameraScreen` dentro de `MainActivity.kt`.
La firma del parámetro es `onFrame: ((CameraFrame) -> Unit)?`; por defecto es `null`.
Conecta ahí el analizador real cuando esté listo. Este ejemplo **solo registra
metadatos** para comprobar la entrega; no detecta manos:

```kotlin
CameraScreen(
    tituloLeccion = video.titulo,
    onBack = { camaraAbierta = false },
    onFrame = { frame ->
        android.util.Log.d(
            "HandsOnFrame",
            "${frame.bitmap.width}x${frame.bitmap.height}, " +
                "giro=${frame.rotationDegrees}, frontal=${frame.isFrontCamera}, " +
                "tiempo=${frame.timestampMillis}"
        )
    }
)
```

En Logcat filtra por `HandsOnFrame` para comprobar que llegan imágenes de la cámara.
Si quieres una prueba del canal de errores, reemplaza temporalmente ese log por
`error("Prueba del consumidor")`: debe aparecer el aviso y permitir reintentar.
Retira ese código al terminar la prueba.

### Contrato del frame

| Campo | Significado |
| --- | --- |
| `bitmap` | Bitmap ARGB_8888 sin rotación aplicada ni espejo; solo válido durante el callback. |
| `rotationDegrees` | Giro horario de 0, 90, 180 o 270 grados necesario para orientar la imagen. |
| `timestampMillis` | Reloj monotónico en milisegundos desde el arranque, no fecha/hora. |
| `isFrontCamera` | Si procede de la cámara frontal. |

La resolución solicitada es 640 × 480, con alternativa según el hardware. Usa
el ancho y alto reales del bitmap, sin asumir un tamaño fijo. Se entregan hasta
10 imágenes por segundo. El procesamiento es secuencial en un hilo de fondo;
si tarda más, CameraX conserva la imagen más reciente y descarta las anteriores.

**Vida de la imagen:** `onFrame` debe consumirla de manera síncrona. Al retornar,
la cámara recicla el bitmap y cierra siempre el `ImageProxy`, incluso si ocurre
una excepción. Si usa una API asíncrona, tu compañera debe copiar el bitmap antes
de retornar, limitar el número de imágenes en curso y liberar cada copia al
completarse su análisis. No pasar directamente el bitmap prestado a `detectAsync`.
La cámara se encarga de cerrar el `ImageProxy`; el consumidor no recibe ese objeto.

**Orientación y espejo:** la vista frontal es espejo para facilitar la práctica;
las imágenes de análisis no se reflejan. El futuro detector debe aplicar el
giro una sola vez (o pasarlo como opción de procesamiento). Si después dibujan
puntos sobre la vista previa, tendrán que transformar sus coordenadas según
rotación, escala, bandas y espejo; esta integración todavía no dibuja puntos.

**Resultados:** este contrato entrega imágenes; tu compañera debe publicar sus
resultados mediante un ViewModel/StateFlow o volver al hilo principal para
actualizar la interfaz. No debe modificar Views desde `onFrame`. Un análisis
ya iniciado puede terminar después de salir: el consumidor debe descartar
resultados de sesiones anteriores y liberar sus propios recursos/modelos.

## Incorporarlo a su repositorio de trabajo

El ZIP no contiene historial Git y no se han publicado cambios en GitHub.
Incluye `CAMARA.patch`, con los cambios respecto del ZIP original.

Si tu copia local todavía coincide con el ZIP, crea una rama para tu trabajo,
copia `CAMARA.patch` a la raíz de tu repositorio y ejecuta desde esa raíz:

```bash
git switch -c camara
git apply --check CAMARA.patch
git apply CAMARA.patch
```

Si la comprobación reporta conflictos por cambios de tus compañeras, combina
los archivos de la tabla manualmente. No reemplaces todo su proyecto actual
por esta copia. El parche es una alternativa para incorporar los cambios al
repositorio original: **no lo apliques dentro de este ZIP, donde ya están hechos**.

## Validación y pruebas pendientes

Se revisaron las dependencias, el manifiesto, las rutas, la liberación de
recursos y los cambios respecto del ZIP original. La revisión estructural no
sustituye la compilación ni la prueba en un teléfono.

Se intentó ejecutar `:app:assembleDebug :app:testDebugUnitTest`; el entorno no
pudo descargar la distribución de Gradle por una restricción de red. No se
completaron la compilación, las pruebas unitarias ni una prueba en dispositivo.
No se generó un APK. El registro antiguo de `INTEGRACION.txt` describe una
validación anterior y no valida estos cambios nuevos.

Ejecuta en Android Studio o en la terminal del proyecto:

```bash
# macOS/Linux
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug

# PowerShell/Windows
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Si Linux no permite ejecutar el wrapper, usa `bash gradlew` o `chmod +x gradlew`.
Las pruebas unitarias que trae el proyecto son de plantilla: no verifican la cámara.

| Prueba manual | Resultado esperado |
| --- | --- |
| Entrar por primera vez | Explicación y botón para pedir permiso; el resto de la app sigue accesible. |
| Denegar el permiso | Aviso, opción de volver a solicitar y acceso a Configuración. |
| Denegar repetidamente | Configuración permite habilitar el permiso cuando Android ya no pregunta. |
| Dar permiso desde Configuración | Al volver aparece la cámara sin reiniciar la app. |
| Girar a vertical, horizontal y 180° | Vista orientada; metadatos de rotación se actualizan. |
| Cambiar de cámara varias veces | Cambia la imagen y la etiqueta sin acumular sesiones. |
| Dispositivo con una sola cámara | Se usa la disponible; cambiar cámara queda deshabilitado. |
| Volver a la lección o ir a Desafío | Se cierra la cámara y se conserva la navegación previa. |
| Inicio/bloquear y luego regresar | El uso de cámara se detiene y se abre de nuevo al volver. |
| Revocar permiso fuera de la app | Al volver se pide habilitarlo otra vez, sin bloquear las lecciones. |
| Volver rápidamente mientras abre | No se abre una cámara en una pantalla que ya se abandonó. |
| Cámara ocupada o sin cámara | Aparece un error recuperable; se puede volver o reintentar. |
| Conectar el callback de Logcat | Imágenes con tamaños reales, giro y tiempo; sin imágenes guardadas. |
| Callback que arroja una excepción | Aviso recuperable, sin dejar imágenes abiertas ni congelar el preview. |

## Referencias técnicas

- [CameraX: versiones](https://developer.android.com/jetpack/androidx/releases/camera)
- [CameraX: análisis de imágenes](https://developer.android.com/media/camera/camerax/analyze)
- [ImageProxy y conversión a Bitmap](https://developer.android.com/reference/androidx/camera/core/ImageProxy)
- [Permisos en tiempo de ejecución](https://developer.android.com/training/permissions/requesting)
