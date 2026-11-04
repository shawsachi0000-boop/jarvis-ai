package com.javris.assistant

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) {
            tts?.language = Locale("hi", "IN")
        }

        setContent {
            var message by remember { mutableStateOf("Hello! I am JAVRIS.") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF050816), Color(0xFF101B38))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "J A V R I S",
                        color = Color.White,
                        fontSize = 32.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF3949AB),
                                        Color(0xFF101B38)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("J", color = Color.White, fontSize = 70.sp)
                    }

                    Text(
                        message,
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Button(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            intent.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")

                            try {
                                startActivityForResult(intent, 100)
                            } catch (e: Exception) {
                                message = "Speech recognition is not available"
                            }
                        }
                    ) {
                        Text("🎙  TALK TO JAVRIS")
                    }

                    Text(
                        "Your Personal Voice Assistant",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val spoken = data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: return

            val reply = when {
                spoken.contains("समय") || spoken.contains("time", true) ->
                    "Abhi time hai " +
                    java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(java.util.Date())

                spoken.contains("नाम") || spoken.contains("name", true) ->
                    "Mera naam JAVRIS hai."

                spoken.contains("हेलो") || spoken.contains("hello", true) ->
                    "Hello! Kaise madad karun?"

                else -> "Aapne kaha: $spoken. Main abhi seekh raha hoon."
            }

            tts?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "javris")
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
