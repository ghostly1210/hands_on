package com.uvg.bienvenida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.uvg.bienvenida.ui.theme.HandsonTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HandsonTheme {
                WelcomeScreen()
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
            painter = painterResource(id = R.drawable.que),
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
            Text("¡Echa un vistazo!")
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

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    HandsonTheme {
        WelcomeScreen()
    }
}
