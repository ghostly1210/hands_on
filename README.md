# HandsOn 👋

HandsOn es una aplicación Android orientada al aprendizaje de la lengua de señas de Guatemala (Lensegua). Combina lecciones con videos, retos diarios y práctica mediante la cámara del dispositivo.

El proyecto busca ofrecer una experiencia de aprendizaje accesible e interactiva para personas de distintas edades.

## 💡 Características principales

- **Lecciones con videos:** catálogo de palabras y expresiones para observar e imitar las señas.
- **Práctica con cámara:** vista en vivo, cambio entre cámara frontal y trasera y gestión de permisos.
- **Reconocimiento de señas:** detección de puntos de las manos y del cuerpo mediante MediaPipe, seguida de una clasificación con TensorFlow Lite.
- **Reto diario:** pregunta de selección múltiple con un intento por día y puntos guardados localmente.
- **Cuentas de usuario:** registro, inicio de sesión, recuperación de contraseña y cierre de sesión mediante Firebase Authentication.
- **Modo invitado:** permite explorar la aplicación sin crear una cuenta.
- **Tema claro y oscuro:** apariencia adaptada a la configuración del dispositivo.

## 💻 Tecnologías utilizadas

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose y Material 3 |
| Entorno de desarrollo | Android Studio |
| Cámara | CameraX |
| Detección de manos y postura | MediaPipe |
| Clasificación de señas | TensorFlow Lite |
| Autenticación | Firebase Authentication |
| Persistencia del reto diario | SharedPreferences |

## 📷 Funcionamiento del reconocimiento

1. El usuario abre una lección y selecciona **Practicar con cámara**.
2. Autoriza el acceso a la cámara e inicia un intento.
3. MediaPipe extrae puntos de las manos y del cuerpo de las imágenes recibidas.
4. La aplicación conserva temporalmente una secuencia de características en memoria.
5. Al seleccionar **Evaluar seña**, el modelo procesa treinta muestras de la secuencia y muestra la etiqueta con mayor puntuación.

Las imágenes de la práctica se procesan en memoria. La aplicación no guarda fotografías ni graba un archivo de video del usuario.

## ⚠️ Alcance actual

- El modelo clasifica nueve etiquetas: **a, e, i, o, u, escuela, gracias, mamá y ¿cuál es tu nombre?**
- La predicción todavía no se compara automáticamente con la seña esperada de la lección.
- La puntuación mostrada corresponde a la salida del modelo; no representa una medición de la precisión general de la aplicación.
- Los puntos del reto diario se guardan en el dispositivo y todavía no se sincronizan con Firebase ni se separan por cuenta.
- El reto utiliza la fecha del teléfono y repite el contenido configurado cada día.
- Algunas lecciones todavía tienen videos pendientes.

## 📁 Organización del código

Los archivos Kotlin se encuentran en `app/src/main/java/com/uvg/bienvenida/`.

| Archivo o carpeta | Responsabilidad |
|---|---|
| `MainActivity.kt` | Inicio, navegación y pantalla de cuenta |
| `EntradaScreen.kt` | Bienvenida y formularios de autenticación |
| `AuthViewModel.kt` | Estado y operaciones de Firebase Authentication |
| `RetoDiarioScreen.kt` | Reto diario, puntos y reproductor de videos |
| `ui/lecciones/` | Pantallas y estado de las lecciones |
| `ui/camara/` | Cámara y reconocimiento de señas |
| `ui/theme/` | Colores y tipografía |

Los modelos se encuentran en `app/src/main/assets/` y los videos en `app/src/main/res/raw/`.

## 🖼️ Vista previa

Próximamente se agregarán capturas de pantalla de la aplicación.
