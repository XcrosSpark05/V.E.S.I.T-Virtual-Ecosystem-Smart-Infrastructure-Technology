package com.example.vesit.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vesit.features.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

data class DashboardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgColor: Color,
    val accentColor: Color,
    val route: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName?.split(" ")?.get(0) ?: "Student"

    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Scaffold(containerColor = Color(0xFFFBFBFF)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- WELCOME HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD))
                            .clickable { navController.navigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF1976D2), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(greeting, fontSize = 14.sp, color = Color.Gray)
                        Text(userName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                IconButton(
                    onClick = { /* Notifications */ },
                    modifier = Modifier.size(48.dp).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                ) { Icon(Icons.Default.NotificationsNone, null) }
            }

            // --- DYNAMIC LIVE LECTURE HERO ---
            UpcomingLectureHero(navController)

            Text("Academic Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // --- GRID ITEMS (Cleaned 3x2 Grid) ---
            val gridItems = listOf(
                DashboardItem("Attendance", "Mark Now", Icons.Default.FactCheck, Color(0xFFE3F2FD), Color(0xFF1976D2), "attendance_details"),
                DashboardItem("Exams", "Results", Icons.Default.Assignment, Color(0xFFFCE4EC), Color(0xFFC2185B), "exams"),
                DashboardItem("Schedule", "Timings", Icons.Default.EventNote, Color(0xFFFFF3E0), Color(0xFFF57C00), "timetable"),
                DashboardItem("Notes", "Materials", Icons.Default.AutoStories, Color(0xFFE8F5E9), Color(0xFF388E3C), "notes"),
                DashboardItem("Canteen", "Menu", Icons.Default.Restaurant, Color(0xFFF3E5F5), Color(0xFF7B1FA2), "canteen"),
                DashboardItem("Issues", "Raise Ticket", Icons.Default.ReportProblem, Color(0xFFFFEBEE), Color(0xFFD32F2F), "complaints")
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                gridItems.forEach { item ->
                    DashboardCard(item = item, modifier = Modifier.weight(1f)) {
                        navController.navigate(item.route)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HeroShareCard { navController.navigate("offline_share") }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UpcomingLectureHero(navController: NavController) {
    // Subtle pulse animation for the Live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse), label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFF6200EE).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(52.dp).graphicsLayer(scaleX = scale, scaleY = scale).background(Color(0xFF6200EE).copy(alpha = 0.1f), CircleShape))
                Icon(Icons.Default.Podcasts, null, tint = Color(0xFF6200EE), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                    Text("LIVE NOW", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Text("Java Full Stack", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1A1C1E))
                Text("Lab 402 • 10:30 - 12:30", fontSize = 13.sp, color = Color.Gray)
            }

            Button(
                onClick = { navController.navigate("attendance_details") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Join", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardCard(item: DashboardItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(28.dp), ambientColor = item.accentColor, spotColor = item.accentColor),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.accentColor)
            }
            Column {
                Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(item.subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun HeroShareCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF6200EE), Color(0xFFBB86FC))))
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Share with Friends", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Transfer materials offline", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}