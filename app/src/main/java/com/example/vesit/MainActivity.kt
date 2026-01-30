package com.example.vesit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vesit.features.auth.LoginScreen
import com.example.vesit.ui.DashboardScreen
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.vesit.features.attendance.AttendanceScreen

import com.example.vesit.ui.theme.VESITTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val user = FirebaseAuth.getInstance().currentUser
            SideEffect {
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = true
            }
            VESITTheme {
                // Scaffold provides the top-level structure (like bars and paddings)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0)
                ) { innerPadding ->

                // Wrap the NavHost in a Box or Column to apply innerPadding
                    NavHost(
                        navController = navController,
                        startDestination = if (user == null) "login" else "dashboard",
                        modifier = Modifier.padding(innerPadding) // Important to handle system bars
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel(),
                                navController = navController // Pass the controller here!
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(navController = navController) // Pass the controller
                        }
                        composable("attendance_details") { AttendanceScreen(navController) }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VESITTheme {
        Greeting("Android")
    }
}