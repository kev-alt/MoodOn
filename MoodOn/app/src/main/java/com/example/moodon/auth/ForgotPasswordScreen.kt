package com.example.moodon.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodon.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    val base = Color(0xFF264653)

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli (hata yapmaması için try-catch önerilebilir)
        Image(
            painter = painterResource(id = R.drawable.forgot),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom // 🟢 alt hizalama
        ) {

            Spacer(Modifier.height(24.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(text = "E-posta", color = base) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = forgotTextFieldColors(base),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Şifre sıfırlama bağlantısı gönderildi",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    coroutineScope.launch {
                                        delay(1000)
                                        navController.popBackStack()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Hata: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Lütfen e-posta girin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = base)
            ) {
                Text(
                    text = "Sıfırlama Bağlantısı Gönder",
                    fontSize = 16.sp,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(180.dp)) // alt boşluk
        }
    }
}

@Composable
private fun forgotTextFieldColors(base: Color): TextFieldColors =
    TextFieldDefaults.colors(
        focusedContainerColor = White.copy(alpha = 0.6f),
        unfocusedContainerColor = White.copy(alpha = 0.6f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFF3AB4BA),
        focusedTextColor = base,
        unfocusedTextColor = base,
        focusedPlaceholderColor = base,
        unfocusedPlaceholderColor = base
    )
