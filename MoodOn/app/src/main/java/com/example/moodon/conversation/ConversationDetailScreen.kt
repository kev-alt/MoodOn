package com.example.moodon.conversation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodon.R
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.entity.PastConversationEntity
import com.example.moodon.data.remote.model.ChatMessage
import com.example.moodon.ui.theme.DarkTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(navController: NavController, conversationId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var conversation by remember { mutableStateOf<PastConversationEntity?>(null) }
    var userName by remember { mutableStateOf("Sen") }

    LaunchedEffect(Unit) {
        val dao = AppDatabase.getInstance(context).conversationDao()
        val profileDao = AppDatabase.getInstance(context).userProfileDao()

        conversation = withContext(Dispatchers.IO) {
            dao.getConversationById(conversationId)
        }

        conversation?.userId?.let { uid ->
            val profile = withContext(Dispatchers.IO) {
                profileDao.getUserByUid(uid)
            }
            profile?.firstName?.takeIf { it.isNotBlank() }?.let {
                userName = it.trim()
            }
        }
    }

    conversation?.let { conv ->
        val formattedDate = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr"))
            .format(Date(conv.timestamp))

        val messages = remember(conv.content) {
            conv.content.lineSequence().mapNotNull { line ->
                val parts = line.split(":", limit = 2)
                if (parts.size == 2)
                    ChatMessage(userId = conv.userId, role = parts[0].trim(), content = parts[1].trim())
                else null
            }.toList()
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(formattedDate, fontSize = 20.sp, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkTeal)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            val isUser = msg.role == "user"
                            ChatBubble(
                                message = msg,
                                isUser = isUser,
                                userName = if (isUser) userName else "Dr. Moo"
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                AppDatabase.getInstance(context)
                                    .conversationDao()
                                    .deleteConversationById(conversationId)
                                Toast
                                    .makeText(context, "Konuşma silindi", Toast.LENGTH_SHORT)
                                    .show()
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp, bottom = 80.dp)
                            .width(160.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTeal)
                    ) {
                        Text("Konuşmayı Sil", color = Color.White)
                    }
                }
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
