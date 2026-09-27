package com.uvg.bienvenida

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uvg.bienvenida.ui.theme.HandsonTheme

data class Lecciones(val id: Int, val nombre: String)
enum class LessonStatus { COMPLETED, CURRENT, LOCKED }
data class Lesson(val id: String, val title: String, val status: LessonStatus)

// Ejemplos temporal (Sin iconos por ahora como placeholders)
val sampleLessons: List<Lesson> = listOf(
    Lesson("saludos", "Saludos", LessonStatus.COMPLETED),
    Lesson("numeros", "Números", LessonStatus.COMPLETED),
    Lesson("comida", "Comida", LessonStatus.CURRENT),
    Lesson("lugares", "Lugares", LessonStatus.LOCKED),
    Lesson("verbos", "Verbos", LessonStatus.LOCKED),
    Lesson("animales", "Animales", LessonStatus.LOCKED),
    Lesson("colores", "Colores", LessonStatus.LOCKED),
    Lesson("familia", "Familia", LessonStatus.LOCKED),
    Lesson("preguntas", "Preguntas", LessonStatus.LOCKED),
    Lesson("emociones", "Emociones", LessonStatus.LOCKED),
    Lesson("tiempo", "Tiempo", LessonStatus.LOCKED),
    Lesson("ropa", "Ropa", LessonStatus.LOCKED)
)

// Colores
private val CompletedGreen = Color(0xFF2E9E5B)
private val CurrentTeal = Color(0xFF0D5C63)
private val CurrentTealRing = Color(0xFFBFE0E2)
private val LockedGray = Color(0xFFE3E3E3)
private val LockedIconGray = Color(0xFF9E9E9E)
private val ScreenBackground = Color(0xFFF5F5F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HandsonTheme {
                LessonsScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HandsOn",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tu puente hacia la comunidad sorda de Guatemala.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.logoQuetzal),
            contentDescription = "Mascota HandsOn",
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Text("Empezar")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { }
        ) {
            Text(stringResource(R.string.echa_un_vistazo))
        }
        Spacer(modifier = Modifier.height(24.dp))
        val annotatedString = buildAnnotatedString {
            append("¿No tienes cuenta? ")
            pushStringAnnotation(tag = "signup", annotation = "signup")
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("Regístrate")
            }
            pop()
        }
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "signup", start = offset, end = offset)
                    .firstOrNull()?.let {
                        // Logica para futura pantalla
                    }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    regionName: String = "Altiplano Central",
    lessons: List<Lesson> = sampleLessons,
    onLessonClick: (Lesson) -> Unit = {}
) {
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("HandsOn", fontWeight = FontWeight.Bold, color = CurrentTeal) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        // Usamos un icono básico de Material3 que suele estar disponible
                        Text("M", fontWeight = FontWeight.Bold, color = CurrentTeal)
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFFFFE8D6),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "\uD83D\uDD25 12",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Box(modifier = Modifier
                        .size(24.dp)
                        .background(Color.Gray)) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Box(modifier = Modifier
                        .size(24.dp)
                        .background(CurrentTeal)) },
                    label = { Text("Mapa") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Box(modifier = Modifier
                        .size(24.dp)
                        .background(Color.Gray)) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "REGIÓN ACTUAL",
                style = MaterialTheme.typography.labelMedium,
                color = CurrentTeal,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = regionName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .width(56.dp)
                    .height(3.dp)
                    .background(CurrentTeal)
            )
            Spacer(modifier = Modifier.height(24.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = lessons,
                    key = { lesson -> lesson.id }
                ) { lesson ->
                    LessonNode(
                        lesson = lesson,
                        onClick = onLessonClick
                    )
                }
            }
            NextStopCard(
                imageUrl = "", // Vacio para usar placeholder
                title = "Costa del Pacífico",
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Composable
fun LessonNode(
    lesson: Lesson,
    onClick: (Lesson) -> Unit
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = lesson.status != LessonStatus.LOCKED) {
                onClick(lesson)
                Toast.makeText(context, "Abriendo lección: ${lesson.title}", Toast.LENGTH_SHORT).show()
            }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (lesson.status == LessonStatus.CURRENT) CurrentTealRing else Color.Transparent
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            when (lesson.status) {
                                LessonStatus.COMPLETED -> CompletedGreen
                                LessonStatus.CURRENT -> CurrentTeal
                                LessonStatus.LOCKED -> LockedGray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder para el icono
                    Box(modifier = Modifier
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.5f)))
                }
            }
            if (lesson.status == LessonStatus.CURRENT) {
                Surface(
                    color = Color(0xFFE0393E),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "HOY",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (lesson.status == LessonStatus.LOCKED) LockedIconGray else Color(0xFF212121)
        )

    }
}

@Composable
fun NextStopCard(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder para la imagen de Next Stop
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PRÓXIMA PARADA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            // Icono de candado placeholder
            Box(modifier = Modifier
                .size(20.dp)
                .background(Color.Gray.copy(alpha = 0.5f)))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    HandsonTheme {
        WelcomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonsScreenPreview() {
    HandsonTheme {
        LessonsScreen()
    }
}
