package com.example.moodon.conversation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodon.R
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.entity.PastConversationEntity
import com.example.moodon.ui.theme.DarkTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastConversationScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    var pastConversations by remember { mutableStateOf<List<PastConversationEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        val dao = AppDatabase.getInstance(context).conversationDao()
        pastConversations = withContext(Dispatchers.IO) {
            dao.getPastConversations(userId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Geçmiş Konuşmalar",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkTeal
                )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pastConversations) { convo ->
                    val formattedDate = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr"))
                        .format(Date(convo.timestamp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("conversation_detail/${convo.id}")
                            }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
                            Text(
                                text = formattedDate,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTeal
                            )
                        }
                    }
                }
            }
        }
    }
}
