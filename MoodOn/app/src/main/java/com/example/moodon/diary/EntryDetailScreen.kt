package com.example.moodon.diary

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.moodon.R
import com.example.moodon.data.local.entity.DiaryEntry
import com.example.moodon.ui.theme.DarkTeal
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EntryDetailScreen(navController: NavController, encodedDate: String) {
    val context = LocalContext.current
    val viewModel = remember { DiaryViewModel(context) }
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val date = java.net.URLDecoder.decode(encodedDate, "UTF-8")
    var entry by remember { mutableStateOf<DiaryEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val fontSizeSp = 28.sp
    val density = LocalDensity.current
    val lineHeightPx = with(density) { fontSizeSp.toPx() * 1.25f }
    val lineHeightSp = with(density) { lineHeightPx.toSp() }

    LaunchedEffect(uid, date) {
        entry = viewModel.getEntry(uid, date)
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkTeal, strokeWidth = 4.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = date,
                            fontSize = 22.sp,
                            color = DarkTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Divider(
                            color = DarkTeal,
                            thickness = 1.dp,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }

                Text(
                    text = "Sevgili Günlük...",
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontFamily = FontFamily.Cursive,
                        color = DarkTeal
                    ),
                    modifier = Modifier.padding(start = 16.dp, top = 32.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineCount = (size.height / lineHeightPx).toInt()
                        for (i in 1..lineCount) {
                            val y = i * lineHeightPx + 6f
                            drawLine(
                                color = Color.Black.copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    BasicTextField(
                        value = entry?.content ?: "",
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 4.dp, top = 6.dp),
                        textStyle = TextStyle(
                            color = DarkTeal,
                            fontSize = fontSizeSp,
                            lineHeight = lineHeightSp,
                            fontFamily = FontFamily.Cursive
                        ),
                        cursorBrush = SolidColor(DarkTeal),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
                        decorationBox = { innerTextField -> innerTextField() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            entry?.let {
                                viewModel.deleteEntry(it)
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Sil", color = Color.White)
                }
            }
        }
    }
}
