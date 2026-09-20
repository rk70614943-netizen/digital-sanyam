package com.rk70614943.digitalsanyam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var onSpeechResult: ((String) -> Unit)? = null

    private val microphonePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("hi", "IN")
            }
        }
        setContent {
            MaterialTheme {
                VoiceAssistantApp(
                    onListen = { result ->
                        onSpeechResult = result
                        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            startListening()
                        } else {
                            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onSpeak = { speak(it) }
                )
            }
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) onSpeechResult?.invoke(text)
                }
                override fun onError(error: Int) { onSpeechResult?.invoke("मुझे आवाज़ साफ़ सुनाई नहीं दी। फिर से बोलिए।") }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            })
        }
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "digital-sanyam-reply")
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun VoiceAssistantApp(
    onListen: ((String) -> Unit) -> Unit,
    onSpeak: (String) -> Unit
) {
    var answer by remember { mutableStateOf("नमस्ते! मैं आपका डिजिटल संयम सहायक हूँ। मुझसे बोलकर कुछ भी पूछिए।") }
    var listening by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        onSpeak(answer)
        onDispose { }
    }

    fun processQuestion(question: String) {
        listening = false
        answer = smartReply(question)
        onSpeak(answer)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("डिजिटल संयम", style = MaterialTheme.typography.headlineMedium)
        Text("आपका हमेशा उपलब्ध आवाज़ सहायक", style = MaterialTheme.typography.titleMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text(answer, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                if (listening) LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        listening = true
                        progress = 0.5f
                        onListen(::processQuestion)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (listening) "सुन रहा हूँ…" else "🎙️ बोलकर पूछें")
                }
            }
        }

        Text("सर्च बार नहीं है। प्रश्न बोलें और सहायक आ��ाज़ में जवाब देगा।")
        Text("माइक्रोफोन अनुमति देने के बाद ही आवाज़ की सुविधा चलेगी।")
        Text("यह सहायक सामान्य वेलनेस जानकारी देता है; डॉक्टर का विकल्प नहीं है।", style = MaterialTheme.typography.bodySmall)
    }
}

fun smartReply(question: String): String {
    val text = question.lowercase(Locale.getDefault())
    return when {
        text.contains("बोर") -> "बोरियत सामान्य है। फोन नीचे रखें और तीन मिनट कमरे में चलें।"
        text.contains("तनाव") || text.contains("टेंशन") -> "चार सेकंड साँस अंदर लें और छह सेकंड बाहर छोड़ें। इसे चार बार दोहराएँ।"
        text.contains("नींद") -> "सोने से तीस मिनट पहले फोन दूर रखें और धीमी रोशनी में आराम करें।"
        text.contains("इंस्टा") || text.contains("सोशल") -> "सोशल मीडिया खोलने से पहले पूछें: मैं यहाँ किस काम के लिए आया हूँ?"
        text.contains("क्या कर") || text.contains("मदद") -> "पहले अपनी इच्छा को एक से दस तक बताइए। फिर मैं आपके लिए छोटा कार्य सुझाऊँगा।"
        else -> "मैंने आपकी बात सुनी। अभी एक मिनट रुककर गहरी साँस लें। इस शुरुआती संस्करण में मैं स्वास्थ्य, आदत और डिजिटल संयम से जुड़े सवालों का जवाब दे सकता हूँ।"
    }
}
