package com.example.vesit.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vesit.features.attendance.AttendanceViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val attendanceViewModel: AttendanceViewModel = viewModel()
    val context = LocalContext.current
    val status by attendanceViewModel.attendanceStatus.collectAsState()

    // 1. Define the Permission Launcher at the top level
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            attendanceViewModel.markAttendanceWithProximity(context)
        } else {
            Toast.makeText(context, "Bluetooth & Location required for Attendance", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("VESIT Nexus", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Welcome back,", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            Text(text = user?.displayName ?: "Student", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    DashboardCard(
                        title = "Attendance",
                        icon = Icons.Default.CheckCircle,
                        status = "Mark Now",
                        onClick = {
                            // 2. Trigger permission check before scanning
                            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            } else {
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            permissionLauncher.launch(permissions)
                        }
                    )
                }
                item {
                    DashboardCard("Payments", Icons.Default.AccountBalanceWallet, "PaySetu", onClick = {})
                }
                item {
                    DashboardCard("LMS", Icons.Default.Book, "3 Pending", onClick = {})
                }
                item {
                    DashboardCard("Events", Icons.Default.Notifications, "Check Now", onClick = {})
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: ImageVector, status: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = status, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}