package com.rk70614943.digitalsanyam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { DigitalSanyamApp() } }
    }
}

data class Task(
    val title: String,
    val description: String,
    val emoji: String,
    val completed: Boolean = false
)

@Composable
fun DigitalSanyamApp() {
    var urgeLevel by remember { mutableFloatStateOf(5f) }
    var message by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("नमस्ते! जब भी फोन चलाने की इच्छा हो, मुझे बताइए।") }
    val tasks = remember {
        mutableStateListOf(
            Task("साँस लेने की कसरत", "3 मिनट धीरे-धीरे साँस लें", "🫁"),
            Task("थोड़ी देर चलें", "5 मिनट फोन से दूर चलें", "🚶"),
            Task("पानी पिएँ", "एक गिलास पानी पिएँ", "💧"),
            Task("आँखों को आराम दें", "20 सेकंड दूर देखें", "👀")
        )
    }
    val completed = tasks.count { it.completed }
    val progress = completed.toFloat() / tasks.size.toFloat()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("डिजिटल संयम", style = MaterialTheme.typography.headlineMedium)
            Text("फोन पर अपना नियंत्रण वापस पाएँ")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("आज का डिजिटल बगीचा", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(if (completed == 0) "🌱" else if (completed < 3) "🌿" else if (completed < 4) "🌳" else "🌲", style = MaterialTheme.typography.displayLarge)
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("$completed/${tasks.size} गतिविधियाँ पूरी")
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("फोन चलाने की इच्छा", style = MaterialTheme.typography.titleLarge)
                    Text("स्तर: ${urgeLevel.toInt()}/10")
                    Slider(value = urgeLevel, onValueChange = { urgeLevel = it }, valueRange = 1f..10f, steps = 8)
                    Button(
                        onClick = {
                            val index = tasks.indexOfFirst { !it.completed }
                            if (index >= 0) tasks[index] = tasks[index].copy(completed = true)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("मुझे 5 मिनट का कार्य दें") }
                }
            }
        }
        item { Text("स्वस्थ विकल्प", style = MaterialTheme.typography.titleLarge) }
        itemsIndexed(tasks) { index, task ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(task.emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.titleMedium)
                        Text(task.description, style = MaterialTheme.typography.bodySmall)
                    }
                    Checkbox(checked = task.completed, onCheckedChange = { checked -> tasks[index] = task.copy(completed = checked) })
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("CBT आधारित सहायक", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(reply)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("जैसे: मुझे बोरियत हो रही है") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { reply = assistantReply(message); message = "" },
                        enabled = message.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("संदेश भेजें") }
                }
            }
        }
        item { Text("यह सामान्य वेलनेस सहायता है, डॉक्टर का विकल्प नहीं।") }
    }
}

fun assistantReply(message: String): String {
    val text = message.lowercase()
    return when {
        text.contains("बोर") -> "बोरियत सामान्य है। फोन नीचे रखें और 3 मिनट कमरे में चलें।"
        text.contains("तनाव") || text.contains("टेंशन") -> "धीरे साँस लें: 4 सेकंड अंदर और 6 सेकंड बाहर छोड़ें।"
        text.contains("नींद") -> "सोने से 30 मिनट पहले फोन दूर रखने की कोशिश करें।"
        text.contains("इंस्टा") || text.contains("सोशल") -> "सोशल मीडिया खोलने से पहले पूछें: मैं यहाँ क्यों आया हूँ?"
        else -> "आपने अपनी भावना पहचानने की अच्छी शुरुआत की है। अभी कोई छोटा स्वस्थ कार्य करें।"
    }
}
