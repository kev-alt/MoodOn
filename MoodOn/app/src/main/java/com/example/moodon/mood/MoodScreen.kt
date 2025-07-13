package com.example.moodon.mood

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.moodon.data.local.AppDatabase
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodon.R
import com.example.moodon.data.local.entity.MoodEntity
import com.example.moodon.data.repository.MoodRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MoodScreen(repo: MoodRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val icons = listOf(
        R.drawable.very_happy,
        R.drawable.happy,
        R.drawable.neutral,
        R.drawable.sad,
        R.drawable.very_sad
    )
    val names = listOf("Çok Mutlu", "Mutlu", "Nötr", "Üzgün", "Çok Üzgün")

    val today = LocalDate.now()
    val dateISO = today.format(DateTimeFormatter.ISO_DATE)
    val datePretty = today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr")))

    var selected by remember { mutableStateOf(2) }
    var toast by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repo.getMoodByDate(dateISO)?.let { m ->
            selected = names.indexOf(m.moodName).takeIf { it >= 0 } ?: 2
        }
    }

    LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
            toast = ""
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.moodscreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bugünkü MOOD'un",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF264653)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = datePretty,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Image(
                painter = painterResource(id = icons[selected]),
                contentDescription = names[selected],
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xBBF0F0F0), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 38.sp,
                    color = Color(0xFF6FB96F),
                    modifier = Modifier.clickable { if (selected > 0) selected-- }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    icons.forEachIndexed { i, ic ->
                        Image(
                            painter = painterResource(id = ic),
                            contentDescription = names[i],
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (i == selected) Color(0xFFCCE5F6) else Color.Transparent,
                                    RoundedCornerShape(30.dp)
                                )
                        )
                    }
                }
                Text(
                    text = "–",
                    fontSize = 38.sp,
                    color = Color(0xFFDD5A5A),
                    modifier = Modifier.clickable { if (selected < icons.lastIndex) selected++ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bugün kendini nasıl hissediyorsun?",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF264653)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val mood = MoodEntity(
                        date = dateISO,
                        day = today.dayOfMonth,
                        month = today.monthValue,
                        year = today.year,
                        weekday = today.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.FULL, Locale("tr")
                        ),
                        moodName = names[selected],
                        userId = uid
                    )
                    scope.launch {
                        try {
                            repo.insertMood(mood)
                            toast = "Mood'unuz kaydedildi"
                        } catch (e: Exception) {
                            toast = "Kaydedilemedi: ${e.localizedMessage}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF264653))
            ) {
                Text(text = "Kaydet", color = Color.White)
            }
        }
    }
}
