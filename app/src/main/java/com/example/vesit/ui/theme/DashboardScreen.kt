package com.example.vesit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vesit.features.attendance.AttendanceViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

// Optimized Pastel Palette
val SoftPurple = Color(0xFFE8EAF6)
val SoftPink = Color(0xFFFCE4EC)
val SoftOrange = Color(0xFFFFF3E0)
val SoftGreen = Color(0xFFE8F5E9)
val TextDark = Color(0xFF1A1A1A)

@Composable
fun DashboardScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val attendanceViewModel: AttendanceViewModel = viewModel()
    val context = LocalContext.current
    val firstName = user?.displayName?.split(" ")?.get(0) ?: "Student"

    // Time-based Greeting Logic
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..20 -> "Good Evening"
        else -> "Good Night"
    }

    // Scaffold with zero insets to remove the "too much gap" issue
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding() // Integrates perfectly with the status bar
                .padding(horizontal = 20.dp)
        ) {
            // Modern Custom Header (Replaces TopAppBar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = "$greeting, $firstName",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Notification Circle
                Surface(
                    modifier = Modifier.size(45.dp),
                    shape = CircleShape,
                    color = Color(0xFFF5F5F5),
                    onClick = { /* Notifications */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, null, tint = TextDark, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Text(
                text = "Academic Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Feature Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    WaveFeatureCard(
                        "Attendance", "Mark Now", Icons.Default.CheckCircle, SoftPurple,
                        onClick = { navController.navigate("attendance_details") }
                    )
                }
                item { WaveFeatureCard("Exams", "Results", Icons.Default.Assignment, SoftPink) {} }
                item { WaveFeatureCard("Schedule", "Class Timings", Icons.Default.CalendarToday, SoftOrange) {} }
                item { WaveFeatureCard("Notes", "Study Material", Icons.Default.Description, SoftGreen) {} }
                item { WaveFeatureCard("Canteen", "Menu & Orders", Icons.Default.Restaurant, SoftPurple) {} }
                item { WaveFeatureCard("Complaints", "Raise Issue", Icons.Default.ReportProblem, SoftPink) {} }

                item(span = { GridItemSpan(2) }) {
                    WaveFeatureCard("Share with Friends", "Invite classmates", Icons.Default.Share, SoftGreen, isFullWidth = true) {}
                }

                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}

@Composable
fun WaveFeatureCard(
    title: String,
    status: String,
    icon: ImageVector,
    bgColor: Color,
    isFullWidth: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isFullWidth) 100.dp else 160.dp),
        shape = RoundedCornerShape(28.dp), // Modern soft corners
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // The Wave Pattern logic from reference image
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.75f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.65f,
                        size.width * 0.7f, size.height * 0.95f,
                        size.width, size.height * 0.8f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color.Black.copy(alpha = 0.05f))
            }

            // Top-right circle menu icon
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(32.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.08f)
            ) {
                Icon(Icons.Default.MoreHoriz, null, tint = TextDark, modifier = Modifier.padding(6.dp))
            }

            Column(
                modifier = Modifier.padding(20.dp).align(Alignment.BottomStart)
            ) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(status, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
            }
        }
    }
}