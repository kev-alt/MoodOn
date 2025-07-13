package com.example.moodon.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodon.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val googleClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1037567208131-0t7abt605oshrl362ko793ran3qs7h2q.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { res ->
                    val user = res.user
                    val isNew = res.additionalUserInfo?.isNewUser ?: true
                    if (user == null) {
                        showNotRegistered(context, navController, auth)
                    } else {
                        handleSocialLogin(db, user.uid, isNew, auth, context, navController)
                    }
                }
                .addOnFailureListener { ex ->
                    Log.e("LOGIN", "Google auth error: ${ex.message}")
                    Toast.makeText(context, "Google giriş hatası: ${ex.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google giriş başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("LOGIN", "Google ApiException", e)
        }
    }

    val twitterLogin: () -> Unit = {
        if (activity == null) {
            Toast.makeText(context, "Twitter girişi için aktivite bulunamadı", Toast.LENGTH_LONG).show()
        } else {
            val provider = OAuthProvider.newBuilder("twitter.com")
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { res ->
                    val user = res.user
                    val isNew = res.additionalUserInfo?.isNewUser ?: true
                    if (user == null) {
                        showNotRegistered(context, navController, auth)
                    } else {
                        handleSocialLogin(db, user.uid, isNew, auth, context, navController)
                    }
                }
                .addOnFailureListener { ex ->
                    Log.e("LOGIN", "Twitter login error: ${ex.message}")
                    Toast.makeText(context, "Twitter giriş hatası: ${ex.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    val headlineColor = Color(0xFF264653)

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.loginscreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Giriş Yap", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = headlineColor)

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("E posta", color = headlineColor) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF3AB4BA)) },
                colors = loginTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Şifre", color = headlineColor) },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF3AB4BA)) },
                colors = loginTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (email.isNotBlank() && password.length >= 6) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            }
                            .addOnFailureListener { ex ->
                                val errorCode = (ex as? FirebaseAuthException)?.errorCode
                                when (errorCode) {
                                    "ERROR_USER_NOT_FOUND" -> {
                                        Toast.makeText(context, "Bu e-posta kayıtlı değil. Lütfen kayıt olun.", Toast.LENGTH_LONG).show()
                                        navController.navigate("register")
                                    }
                                    "ERROR_WRONG_PASSWORD" -> {
                                        Toast.makeText(context, "Şifre yanlış", Toast.LENGTH_LONG).show()
                                    }
                                    else -> {
                                        Toast.makeText(context, "Giriş başarısız. Lütfen e-posta ve şifre bilgilerinizi kontrol edin.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(context, "Geçerli e posta ve şifre girin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = headlineColor)
            ) {
                Text("Giriş Yap", fontSize = 16.sp, color = White)
            }

            Text(
                "Şifremi Unuttum",
                fontSize = 14.sp,
                color = Color(0xFF3AB4BA),
                modifier = Modifier
                    .clickable { navController.navigate("forgot_password") }
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Text("- başka bir yöntemle giriş yap -", fontSize = 14.sp, color = headlineColor)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = {
                    googleClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleClient.signInIntent)
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = twitterLogin) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_twitter),
                        contentDescription = "Twitter",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hesabınız yok mu?", fontSize = 20.sp, color = headlineColor)
            Spacer(Modifier.height(4.dp))
            Text(
                "Kayıt Ol",
                fontSize = 18.sp,
                color = Color(0xFF3AB4BA),
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }
    }
}

private fun handleSocialLogin(
    db: FirebaseFirestore,
    uid: String,
    isNew: Boolean,
    auth: FirebaseAuth,
    context: android.content.Context,
    navController: NavController
) {
    if (isNew) {
        auth.currentUser?.delete()
        showNotRegistered(context, navController, auth)
        return
    }

    val userDoc = db.collection("users").document(uid)
    userDoc.get()
        .addOnSuccessListener { snap ->
            if (!snap.exists()) {
                userDoc.set(
                    mapOf(
                        "fullName" to "",
                        "email" to auth.currentUser?.email.orEmpty(),
                        "registeredAt" to Date()
                    )
                )
            }
            Toast.makeText(context, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
            navController.navigate("home")
        }
        .addOnFailureListener {
            Log.e("LOGIN", "Firestore kontrol hatası: ${it.message}")
            Toast.makeText(context, "Giriş başarılı", Toast.LENGTH_LONG).show()
            navController.navigate("home")
        }
}

private fun showNotRegistered(
    context: android.content.Context,
    navController: NavController,
    auth: FirebaseAuth
) {
    Toast.makeText(context, "Bu hesap kayıtlı değil, lütfen kayıt olun", Toast.LENGTH_LONG).show()
    auth.signOut()
    navController.navigate("register")
}

@Composable
fun loginTextFieldColors(): TextFieldColors {
    val base = Color(0xFF264653)
    return TextFieldDefaults.colors(
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
}
