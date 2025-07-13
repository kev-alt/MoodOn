package com.example.moodon.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.moodon.R
import com.example.moodon.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid
    val coroutineScope = rememberCoroutineScope()
    var firstName by remember { mutableStateOf("Kullanıcı") }

    LaunchedEffect(uid) {
        uid?.let {
            coroutineScope.launch {
                try {
                    val db = AppDatabase.getInstance(context).userProfileDao()
                    val profile = withContext(Dispatchers.IO) {
                        db.getUserByUid(uid)
                    }
                    firstName = profile?.firstName
                        ?: currentUser.displayName?.split(" ")?.firstOrNull()
                                ?: currentUser.email?.substringBefore("@")
                                ?: "Kullanıcı"
                } catch (e: Exception) {
                    e.printStackTrace()
                    firstName = "Kullanıcı"
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Arka Plan",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp)
                .zIndex(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "Merhaba, $firstName!",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF264653)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            GradientMenuButton("Hadi Konuşalım") {
                navController.navigate("conversationscreen")
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientMenuButton("Bugünkü Mood'un Nasıl?") {
                navController.navigate("moodscreen")
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientMenuButton("Kendine Bir Not Bırak") {
                navController.navigate("note")
            }
        }

        Image(
            painter = painterResource(id = R.drawable.therapistcow),
            contentDescription = "Terapist İnek",
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-25).dp, y = (-100).dp)
                .align(Alignment.CenterStart)
                .zIndex(2f)
        )
    }
}

@Composable
fun GradientMenuButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(8.dp, shape = RoundedCornerShape(40.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF3B3A6), Color(0xFFAEE2E2))
                ),
                shape = RoundedCornerShape(40.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = Color(0xFF264653),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = if (text == "Hadi Konuşalım") Modifier.padding(start = 70.dp) else Modifier
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF264653).copy(alpha = 0.9f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Devam",
                    tint = Color.White
                )
            }
        }
    }
}
