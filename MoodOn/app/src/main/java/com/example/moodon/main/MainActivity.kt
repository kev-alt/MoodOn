package com.example.moodon.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.moodon.R
import com.example.moodon.auth.*
import com.example.moodon.conversation.*
import com.example.moodon.diary.*
import com.example.moodon.profile.ProfileScreen
import com.example.moodon.ui.theme.MoodOnTheme
import com.example.moodon.data.local.AppDatabase
import com.example.moodon.data.repository.MoodRepository
import com.example.moodon.mood.MoodScreen
import com.example.moodon.mood.PastMoodScreen
import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodOnTheme {
                val navController = rememberNavController()
                val currentRoute by navController.currentBackStackEntryAsState()
                val hideBottomBarRoutes = listOf("welcome", "login", "register", "forgot_password", "new_entry")

                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(navController)
                    val currentPath = currentRoute?.destination?.route
                    val isEntryDetail = currentPath?.startsWith("entry/") == true

                    if (currentPath !in hideBottomBarRoutes && !isEntryDetail) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            TransparentRoundedBottomBar(navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: "default_user"

    // MoodRepository tanımı
    val db = AppDatabase.getInstance(context)
    val moodDao = db.moodDao()
    val moodRepo = remember { MoodRepository(moodDao) }

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("pastmood") { PastMoodScreen(repo = moodRepo) }
        composable("moodscreen") { MoodScreen(repo = moodRepo) }
        composable("timer") {
            PastConversationScreen(navController = navController, userId = userId)
        }
        composable("profile") { ProfileScreen(navController) }
        composable("note") { NotesScreen(navController) }
        composable("new_entry") { NewEntryScreen(navController) }
        composable("conversationscreen") {
            ConversationScreen(userId = userId, navController = navController)
        }
        composable(
            route = "entry/{encodedDate}",
            arguments = listOf(navArgument("encodedDate") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedDate = backStackEntry.arguments?.getString("encodedDate") ?: ""
            val decodedDate = URLDecoder.decode(encodedDate, "UTF-8")
            EntryDetailScreen(navController, decodedDate)
        }
        composable(
            route = "conversation_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val convoId = backStackEntry.arguments?.getInt("id") ?: -1
            ConversationDetailScreen(navController, convoId)
        }
    }
}

@Composable
fun TransparentRoundedBottomBar(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomIcon(R.drawable.ic_home, "home", navController)
            BottomIcon(R.drawable.ic_smile, "pastmood", navController)
            BottomIcon(R.drawable.ic_clock, "timer", navController)
            BottomIcon(R.drawable.ic_profile, "profile", navController)
        }
    }
}

@Composable
fun BottomIcon(iconRes: Int, route: String, navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = route,
        tint = if (currentRoute == route) Color(0xFF2A9D8F) else Color(0xFF264653),
        modifier = Modifier
            .size(26.dp)
            .clickable {
                if (currentRoute != route) {
                    navController.navigate(route) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
    )
}
