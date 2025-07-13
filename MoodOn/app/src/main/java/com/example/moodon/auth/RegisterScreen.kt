package com.example.moodon.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodon.R
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.local.entity.UserProfileEntity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val localDb = AppDatabase.getInstance(context)

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("1037567208131-0t7abt605oshrl362ko793ran3qs7h2q.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleClient = GoogleSignIn.getClient(context, gso)
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { res ->
                    val user = res.user
                    if (res.additionalUserInfo?.isNewUser == true && user != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val entity = UserProfileEntity(
                                uid = user.uid,
                                email = user.email ?: "",
                                firstName = name,
                                lastName = surname,
                                age = age.toIntOrNull() ?: 0,
                                gender = gender
                            )
                            localDb.userProfileDao().insertUser(entity)
                            firestore.collection("users").document(user.uid).set(entity)
                        }
                        Toast.makeText(context, "Google ile kayıt başarılı!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home")
                    } else {
                        Toast.makeText(context, "Bu Google hesabı zaten kayıtlı", Toast.LENGTH_LONG).show()
                        navController.navigate("login")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Google giriş hatası: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google giriş başarısız", Toast.LENGTH_SHORT).show()
        }
    }

    val twitterProvider = OAuthProvider.newBuilder("twitter.com")

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.registerscreen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Hesap Oluştur", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF264653))
            CustomTextField("Ad", name) { name = it }
            CustomTextField("Soyad", surname) { surname = it }
            CustomTextField("E-posta", email, Icons.Default.Email) { email = it }
            CustomTextField("Şifre", password, Icons.Default.Lock, isPassword = true) { password = it }
            CustomTextField("Yaş", age, Icons.Outlined.CalendarToday) { age = it }

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
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = White.copy(alpha = 0.6f),
                        unfocusedContainerColor = White.copy(alpha = 0.6f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF3AB4BA),
                        focusedTextColor = Color(0xFF264653),
                        unfocusedTextColor = Color(0xFF264653)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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

            Button(
                onClick = {
                    if (email.isNotBlank() && password.length >= 6) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { res ->
                                val user = res.user ?: return@addOnSuccessListener
                                CoroutineScope(Dispatchers.IO).launch {
                                    val entity = UserProfileEntity(
                                        uid = user.uid,
                                        email = email,
                                        firstName = name,
                                        lastName = surname,
                                        age = age.toIntOrNull() ?: 0,
                                        gender = gender
                                    )
                                    localDb.userProfileDao().insertUser(entity)
                                    firestore.collection("users").document(user.uid).set(entity)
                                }
                                Toast.makeText(context, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            }
                            .addOnFailureListener { ex ->
                                if (ex is FirebaseAuthUserCollisionException) {
                                    Toast.makeText(context, "Zaten kayıtlı, giriş yapın", Toast.LENGTH_LONG).show()
                                    navController.navigate("login")
                                } else {
                                    Toast.makeText(context, "Hata: ${ex.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Geçerli e posta ve 6+ haneli şifre girin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF264653))
            ) {
                Text("Kayıt Ol", color = White)
            }

            Spacer(Modifier.height(16.dp))
            Text("- başka bir yöntemle kayıt ol -", color = Color(0xFF264653))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = {
                    googleClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleClient.signInIntent)
                    }
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, tint = Color.Unspecified)
                }

                IconButton(onClick = {
                    activity?.let {
                        auth.startActivityForSignInWithProvider(it, twitterProvider.build())
                            .addOnSuccessListener { res ->
                                val user = res.user
                                val isNewUser = res.additionalUserInfo?.isNewUser ?: false
                                if (isNewUser && user != null) {
                                    val uid = user.uid
                                    val displayName = user.displayName ?: "Twitter Kullanıcısı"
                                    val emailResolved = user.email ?: "twitter_$uid@moodon.com"
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val entity = UserProfileEntity(
                                            uid = uid,
                                            email = emailResolved,
                                            firstName = displayName,
                                            lastName = "",
                                            age = 0,
                                            gender = ""
                                        )
                                        localDb.userProfileDao().insertUser(entity)
                                        firestore.collection("users").document(uid).set(entity)
                                    }
                                    Toast.makeText(context, "Twitter ile kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home")
                                } else {
                                    Toast.makeText(context, "Zaten kayıtlı, giriş yapın", Toast.LENGTH_LONG).show()
                                    navController.navigate("login")
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Twitter giriş hatası: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_twitter), contentDescription = null, tint = Color.Unspecified)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Zaten hesabınız var mı?", color = Color(0xFF264653))
            Text("Giriş Yap", color = Color(0xFF3AB4BA), modifier = Modifier.clickable {
                navController.navigate("login")
            })
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Person,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    val color = Color(0xFF264653)
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = color) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF3AB4BA)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = White.copy(alpha = 0.6f),
            unfocusedContainerColor = White.copy(alpha = 0.6f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF3AB4BA),
            focusedTextColor = color,
            unfocusedTextColor = color
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
