package com.example.moodon.conversation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodon.ui.theme.DarkTeal
import com.example.moodon.R
import com.example.moodon.data.remote.model.ChatMessage

val UserBubble = Color(0xFFF3B3A6).copy(alpha = 0.8f)
val BotBubble = Color.White.copy(alpha = 0.8f)

@Composable
fun ChatBubble(
    message: ChatMessage,
    isUser: Boolean,
    userName: String
) {
    val bubbleColor = if (isUser) UserBubble else BotBubble
    val avatar = if (isUser) R.drawable.ic_profile else R.drawable.therapistcow
    val sender = if (isUser) userName else "Dr. Moo"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Text(
                text = sender,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 2.dp,
                color = bubbleColor,
                modifier = Modifier.widthIn(max = 260.dp)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = DarkTeal,
                    fontSize = 16.sp
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(6.dp))
            Image(
                painter = painterResource(id = avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                colorFilter = ColorFilter.tint(DarkTeal)
            )
        }
    }
}
