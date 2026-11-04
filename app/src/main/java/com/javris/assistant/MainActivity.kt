package com.javris.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private var listening by mutableStateOf(false)
    private var heard by mutableStateOf("Tap the microphone and speak.")
    private var ttsReady = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.RECORD_AUDIO] == true) startListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent {
            val bg = Brush.verticalGradient(listOf(Color(0xFF080B14), Color(0xFF17102A), Color(0xFF090D18)))
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFFF334F), secondary = Color(0xFF328BFF))) {
                Box(Modifier.fillMaxSize().background(bg).padding(22.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("JAVRIS", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
                        Text("YOUR PERSONAL AI ASSISTANT", color = Color(0xFF8EA7D7), fontSize = 11.sp, letterSpacing = 3.sp)
                        Box(Modifier.size(190.dp).background(Brush.radialGradient(listOf(Color(0x66FF3150), Color(0x44328BFF), Color.Transparent)), RoundedCornerShape(100.dp)), contentAlignment = Alignment.Center) {
                            Text("J", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Light)
                        }
                        Text(heard, color = Color(0xFFE4E9F5), fontSize = 16.sp)
                        Button(onClick = { requestMicAndListen() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52F4D)), modifier = Modifier.fillMaxWidth()) {
                            Text(if (listening) "LISTENING…" else "🎙 TALK TO JAVRIS", color = Color.White)
                        }
                        OutlinedButton(onClick = { startWakeService() }, modifier = Modifier.fillMaxWidth()) {
                            Text("ENABLE WAKE WORD • JAVRIS", color = Color(0xFF79B7FF))
                        }
                        Text("Female voice depends on voices installed on your phone.", color = Color(0xFF8993A8), fontSize = 11.sp)
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts?.language = Locale("en", "IN")
            val female = tts?.voices.orEmpty().firstOrNull { it.locale.language == "en" && (it.name.contains("female", true) || it.name.contains("woman", true)) }
            if (female != null) tts?.voice = female
        }
    }

    private fun requestMicAndListen() {
        val needed = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) needed.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = needed.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray()) else startListening()
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { heard = "Speech recognition is not available."; return }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true; heard = "I'm listening..." }
            override fun onResults(results: Bundle?) {
                listening = false
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                heard = phrase.ifBlank { "I didn't catch that. Try again." }
                speak(replyFor(phrase))
            }
            override fun onError(error: Int) { listening = false; heard = "Didn't catch that. Tap and try again." }
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onRmsChanged(rmsdB: Float) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer?.startListening(intent)
    }

    private fun replyFor(input: String): String {
        val q = input.lowercase(Locale.ROOT)
        return when {
            q.contains("hello") || q.contains("नमस्ते") || q.contains("हेलो") -> "नमस्ते! मैं JAVRIS हूँ। बताइए, मैं आपकी क्या मदद करूँ?"
            q.contains("who are you") || q.contains("तुम कौन") -> "मैं JAVRIS, आपका personal assistant हूँ।"
            else -> "मैंने सुना: $input. अभी मैं basic voice commands पर काम कर रहा हूँ।"
        }
    }

    private fun speak(message: String) {
        heard = message
        if (ttsReady) tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "javris_reply")
    }

    private fun startWakeService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestMicAndListen(); return
        }
        val intent = Intent(this, WakeWordService::class.java)
        ContextCompat.startForegroundService(this, intent)
        heard = "Wake-word service requested."
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}
