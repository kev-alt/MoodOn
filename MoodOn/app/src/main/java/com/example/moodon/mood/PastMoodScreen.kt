package com.example.moodon.mood

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodon.R
import com.example.moodon.data.local.entity.MoodEntity
import com.example.moodon.data.repository.MoodRepository
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import kotlin.math.roundToInt

private val CardBg = Color.White.copy(alpha = 0.6f)
private val LineColor = Color(0xFF99C47B)
private val TextColor = Color(0xFF264653)
private val DayBoxColors = listOf(
    Color(0xFFFFD1DC), Color(0xFFB2EBF2), Color(0xFFFFF9C4),
    Color(0xFFE1BEE7), Color(0xFFC8E6C9), Color(0xFFFFE0B2), Color(0xFFD1C4E9)
).map { it.copy(alpha = 0.6f) }

private val moodIcons = mapOf(
    "Çok Üzgün" to R.drawable.very_sad,
    "Üzgün" to R.drawable.sad,
    "Nötr" to R.drawable.neutral,
    "Mutlu" to R.drawable.happy,
    "Çok Mutlu" to R.drawable.very_happy
)
private val moodValues = mapOf(
    "Çok Üzgün" to 0, "Üzgün" to 1, "Nötr" to 2, "Mutlu" to 3, "Çok Mutlu" to 4
)

private const val ICON_SIZE = 20
private const val LINE_STROKE_WIDTH = 2f

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PastMoodScreen(repo: MoodRepository) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var last7 by remember { mutableStateOf<List<MoodEntity>>(emptyList()) }

    LaunchedEffect(uid) {
        last7 = repo.getLast7Days()
    }

    val mondayFirst = remember(last7) {
        val neutral = MoodEntity(
            date = "",
            moodName = "Nötr",
            day = 0,
            month = 0,
            year = 0,
            weekday = "",
            userId = uid
        )

        MutableList(7) { neutral }.also { buffer ->
            last7.forEach { mood ->
                val date = LocalDate.parse(mood.date)
                val idx = date.dayOfWeek.ordinal
                buffer[idx] = mood
            }
        }
    }

    val loginCount = mondayFirst.count { it.date.isNotEmpty() }
    val avgValue = mondayFirst.mapNotNull { moodValues[it.moodName] }
        .takeIf { it.isNotEmpty() }?.average()?.roundToInt() ?: 2
    val avgName = moodValues.entries.first { it.value == avgValue }.key
    val avgIcon = moodIcons[avgName] ?: R.drawable.neutral

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.pastmoodscreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StatCard(
                icon = R.drawable.ic_check,
                iconTint = Color(0xFF4CAF50),
                bgColor = Color(0xFFC8E6C9),
                label = "Son 7 günde",
                value = "$loginCount kez giriş yaptınız"
            )

            StatCard(
                customImage = avgIcon,
                label = avgName,
                value = "Son 7 gün ortalaması"
            )

            MoodChart(mondayFirst)

            val days = listOf("PZT", "SALI", "ÇRŞ", "PRŞ", "CUMA", "CTS", "PAZ")
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEachIndexed { i, d ->
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(28.dp)
                            .background(DayBoxColors[i], RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = d,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: Int? = null,
    customImage: Int? = null,
    iconTint: Color = Color.Unspecified,
    bgColor: Color = CardBg,
    label: String,
    value: String
) {
    Box(contentAlignment = Alignment.TopCenter) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColor
                    )
                }
            }
        }

        if (icon != null) {
            Box(
                modifier = Modifier
                    .offset(y = (-22).dp)
                    .size(44.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else if (customImage != null) {
            Image(
                painter = painterResource(customImage),
                contentDescription = null,
                modifier = Modifier
                    .offset(y = (-22).dp)
                    .size(44.dp)
            )
        }
    }
}

@Composable
private fun MoodChart(moods: List<MoodEntity>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(200.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val stepX = maxWidth / 7f
            val stepY = maxHeight / 6f
            val yOffset = stepY

            Canvas(modifier = Modifier.fillMaxSize()) {
                val points = moods.mapIndexed { i, m ->
                    val v = moodValues[m.moodName] ?: 2
                    Offset(
                        stepX.toPx() * (i + 0.5f),
                        size.height - (v * stepY.toPx()) - yOffset.toPx()
                    )
                }
                points.windowed(2) { (p1, p2) ->
                    drawLine(LineColor, p1, p2, LINE_STROKE_WIDTH, StrokeCap.Round)
                }
            }

            moods.forEachIndexed { i, m ->
                val v = moodValues[m.moodName] ?: 2
                val icon = moodIcons[m.moodName] ?: R.drawable.neutral
                val x = stepX * (i + 0.5f)
                val y = maxHeight - (stepY * v) - (ICON_SIZE.dp / 2) - yOffset
                Image(
                    painter = painterResource(icon),
                    contentDescription = m.moodName,
                    modifier = Modifier
                        .size(ICON_SIZE.dp)
                        .offset {
                            IntOffset(x.roundToPx() - ICON_SIZE / 2, y.roundToPx())
                        }
                )
            }
        }
    }
}
