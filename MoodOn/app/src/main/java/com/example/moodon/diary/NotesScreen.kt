package com.example.moodon.diary

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
import com.example.moodon.data.local.entity.DiaryEntry
import com.example.moodon.ui.theme.DarkTeal
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { DiaryViewModel(context) }
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val entries by viewModel.entries.collectAsState()

    LaunchedEffect(uid) {
        viewModel.loadEntries(uid)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Sevgili Günlük",
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries.sortedByDescending { it.date }) { entry ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val encodedDate = URLEncoder.encode(entry.date, "UTF-8")
                                    navController.navigate("entry/$encodedDate")
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = entry.date,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTeal
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { navController.navigate("new_entry") },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp)
                        .width(160.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Günlük Yaz", color = Color.White)
                }
            }
        }
    }
}
