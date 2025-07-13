package com.example.moodon.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Wc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moodon.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun fieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedContainerColor = White.copy(alpha = 0.4f),
        unfocusedContainerColor = White.copy(alpha = 0.4f),
        cursorColor = Color(0xFF3AB4BA),
        focusedTextColor = Color(0xFF264653),
        unfocusedTextColor = Color(0xFF264653),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.registerscreen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        when (uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF264653)
                )
            }

            is ProfileUiState.Error -> {
                Text(
                    text = (uiState as ProfileUiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ProfileUiState.Data -> {
                val state = (uiState as ProfileUiState.Data).profile
                var firstName by remember { mutableStateOf(state.firstName) }
                var lastName by remember { mutableStateOf(state.lastName) }
                var age by remember { mutableStateOf(state.age.toString()) }
                var gender by remember { mutableStateOf(state.gender) }
                var genderExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("PROFİL", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF264653))

                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = { Text("Ad", color = Color(0xFF264653)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3AB4BA))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = { Text("Soyad", color = Color(0xFF264653)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3AB4BA))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = age,
                        onValueChange = { age = it },
                        placeholder = { Text("Yaş", color = Color(0xFF264653)) },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF3AB4BA))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            readOnly = true,
                            value = gender,
                            onValueChange = {},
                            placeholder = { Text("Cinsiyet", color = Color(0xFF264653)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Wc, contentDescription = null, tint = Color(0xFF3AB4BA))
                            },
                            trailingIcon = {
                                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF3AB4BA))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            listOf("Kadın", "Erkek", "Belirtmek istemiyorum").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    TextField(
                        value = state.email,
                        onValueChange = {},
                        placeholder = { Text("E-posta", color = Color(0xFF264653)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3AB4BA))
                        },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.save(
                                    firstName = firstName,
                                    lastName = lastName,
                                    age = age.toIntOrNull() ?: 0,
                                    gender = gender
                                ) { error ->
                                    scope.launch(Dispatchers.Main) {
                                        val msg = if (error == null) "Profil güncellendi" else "Hata: $error"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF264653))
                    ) {
                        Text("Kaydet", color = Color.White)
                    }

                    Text(
                        text = "Şifremi Değiştir",
                        color = Color(0xFF3AB4BA),
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            FirebaseAuth.getInstance().currentUser?.email?.let {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(it)
                                Toast.makeText(context, "Şifre sıfırlama e postası gönderildi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("welcome") {
                            popUpTo("profile") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 100.dp)
                        .width(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF264653))
                ) {
                    Text("Çıkış Yap", color = Color.White)
                }
            }
        }
    }
}
