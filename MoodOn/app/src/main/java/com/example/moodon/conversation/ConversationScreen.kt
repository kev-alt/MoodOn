package com.example.moodon.conversation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moodon.R
import com.example.moodon.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val GradientEnd = Color(0xFFAEE2E2)
private val BottomOffset = 80.dp
private val DarkTeal = Color(0xFF264653)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(userId: String, navController: NavController) {
    val context = LocalContext.current
    val viewModel: ConversationViewModel = viewModel(factory = ConversationViewModelFactory(context.applicationContext))

    val messages by viewModel.chatMessages.collectAsState()
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var userName by remember { mutableStateOf("Sen") }

    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    var isListening by remember { mutableStateOf(false) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            recognizer.startListening(speechIntent)
            isListening = true
        }
    }

    DisposableEffect(Unit) {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                isListening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!spoken.isNullOrBlank()) {
                    viewModel.startConversation(userId, spoken)
                }
            }
            override fun onError(error: Int) { isListening = false }
            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
        onDispose { recognizer.destroy() }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserMessages(userId) // 🔁 Konuşma ekranı terk edilip dönülse bile yüklenir
        val dao = AppDatabase.getInstance(context).userProfileDao()
        val profile = withContext(Dispatchers.IO) { dao.getUserByUid(userId) }
        profile?.firstName?.takeIf { it.isNotBlank() }?.let {
            userName = it.trim()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.therapistcow),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Dr. Moo", fontSize = 24.sp)
                                Text("Çevrim içi", fontSize = 12.sp, color = GradientEnd)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkTeal),
                    actions = {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Filled.Close, contentDescription = "Bitir")
                        }
                    }
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = BottomOffset + 80.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(
                            message = message,
                            isUser = message.role == "user",
                            userName = userName
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = BottomOffset)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Dr. Moo ile konuş…", color = DarkTeal) },
                    textStyle = TextStyle(color = DarkTeal),
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.6f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = DarkTeal
                    ),
                    shape = RoundedCornerShape(40.dp)
                )

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = {
                    if (isListening) {
                        recognizer.stopListening()
                        isListening = false
                    } else {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {
                            recognizer.startListening(speechIntent)
                            isListening = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }) {
                    Icon(Icons.Filled.Mic, contentDescription = "Mikrofon", tint = if (isListening) GradientEnd else DarkTeal)
                }

                IconButton(onClick = {
                    val text = input.text.trim()
                    if (text.isNotEmpty()) {
                        viewModel.startConversation(userId, text)
                        input = TextFieldValue("")
                        focusManager.clearFocus()
                        keyboard?.hide()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", tint = DarkTeal)
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Konuşmayı bitirmek istiyor musun?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.endConversationAndSave(userId) {
                                navController.navigate("home") {
                                    popUpTo("conversationscreen") { inclusive = true }
                                }
                            }
                        }) { Text("Evet") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Hayır") }
                    }
                )
            }
        }
    }
}
