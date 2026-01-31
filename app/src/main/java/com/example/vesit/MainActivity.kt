package com.example.vesit

import android.Manifest
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vesit.features.attendance.AttendanceScreen
import com.example.vesit.features.auth.AuthViewModel
import com.example.vesit.features.auth.LoginScreen
import com.example.vesit.features.canteen.CanteenScreen
import com.example.vesit.features.complaints.ComplaintsScreen
import com.example.vesit.features.exams.ExamsScreen
import com.example.vesit.features.notes.NotesScreen
import com.example.vesit.features.profile.ProfileScreen
import com.example.vesit.features.schedule.TimetableScreen
import com.example.vesit.features.share.OfflineShareScreen
import com.example.vesit.features.teacher.TeacherHomeScreen
import com.example.vesit.ui.DashboardScreen
import com.example.vesit.ui.theme.VESITTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    // Define navController at the class level so onNewIntent can access it
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request essential permissions for offline discovery
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)

        enableEdgeToEdge()
        setContent {
            navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            val user = FirebaseAuth.getInstance().currentUser

            val startRoute = when {
                user == null -> "login"
                user.email?.endsWith("@ves.ac.in") == true -> "dashboard"
                user.email?.endsWith("@gmail.com") == true -> "teacher_home"
                else -> "login"
            }

            SideEffect {
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = true
            }

            VESITTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startRoute,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(viewModel = authViewModel, navController = navController)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("attendance_details") {
                            AttendanceScreen(navController = navController)
                        }
                        composable("teacher_home") {
                            TeacherHomeScreen(navController = navController)
                        }
                        composable("offline_share") {
                            OfflineShareScreen(navController = navController)
                        }

                        // ADD THIS LINE TO FIX THE CRASH
                        composable("complaints") {
                            ComplaintsScreen(navController = navController)
                        }

                        composable("profile") {
                            ProfileScreen(navController = navController)
                        }

                        composable("timetable") {
                            TimetableScreen(navController = navController)
                        }

                        composable("notes") {
                            NotesScreen(navController = navController)
                        }

                        composable("exams") { ExamsScreen(navController) }

                        composable("canteen") { CanteenScreen(navController = navController) }
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onResume() {
        super.onResume()
        // If the app was opened directly by an NFC tap
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            handleNfcIntent(intent)
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Important for onResume to see the new data
        handleNfcIntent(intent)
    }

    // Inside handleNfcIntent function in MainActivity.kt
    // Inside handleNfcIntent in MainActivity.kt
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleNfcIntent(intent: Intent) {
        // LOG 1: Check if any intent reached the app
        Log.d("VESIT_NFC", "Intent Received: ${intent.action}")

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // LOG 2: Handshake confirmed
            Log.d("VESIT_NFC", "Handshake MATCHED! Action: NDEF_DISCOVERED")

            // 1. Physical confirmation (Judge can see/hear this)
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(300)
            }

            // 2. Visual Toast
            Toast.makeText(this, "Handshake Success! Opening Share...", Toast.LENGTH_SHORT).show()

            // 3. Navigation
            if (::navController.isInitialized) {
                navController.navigate("offline_share") { launchSingleTop = true }

            }
        } else {
            // LOG 3: Why it failed
            Log.e("VESIT_NFC", "Tap detected but Action was: ${intent.action}. Check Intent Filter in Manifest!")
        }
    }

}