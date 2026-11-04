package com.javris.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN")
            }
        }

        setContent {
            JavrisApp()
        }
    }

    private fun speak(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "JAVRIS"
        )
    }

    private fun listen(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            onResult("Speech recognition available nahi hai.")
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                onResult("Dobara bolkar dekhiye.")
            }

            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                onResult(words?.firstOrNull() ?: "")
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer?.startListening(intent)
    }

    @Composable
    private fun JavrisApp() {
        var command by remember { mutableStateOf("") }
        var answer by remember { mutableStateOf("Namaste! Main JAVRIS hoon.") }
        var listening by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                listening = true
                listen {
                    command = it
                    listening = false
                }
            }
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color.Cyan,
                background = Color(0xFF070B16),
                surface = Color(0xFF111A2B)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF070B16),
                                Color(0xFF14213D),
                                Color(0xFF080D19)
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "JAVRIS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Cyan
                )

                Text(
                    "PERSONAL AI ASSISTANT",
                    color = Color.LightGray
                )

                Spacer(Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (listening) "LISTENING" else "J",
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(35.dp))

                Text(
                    answer,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.weight(1f))

                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ask JAVRIS") }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        answer = AssistantActions.run(
                            this@MainActivity,
                            command
                        )
                        speak(answer)
                        command = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SEND COMMAND")
                }

                Button(
                    onClick = {
                        if (
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            listening = true
                            listen {
                                command = it
                                listening = false
                            }
                        } else {
                            permissionLauncher.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎙 SPEAK TO JAVRIS")
                }
            }
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}
