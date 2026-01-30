package com.example.vesit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DashboardScreen() {
    val user = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Text(
            text = "Welcome back,",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = user?.displayName ?: "Student",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f) // FIX: Changed from fillWeight to weight
        ) {
            item { DashboardCard("Attendance", Icons.Default.CheckCircle, "85%") }
            item { DashboardCard("Payments", Icons.Default.AccountBalanceWallet, "PaySetu") }
            item { DashboardCard("LMS", Icons.Default.Book, "3 Pending") }
            item { DashboardCard("Events", Icons.Default.Notifications, "Check Now") }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: ImageVector, status: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        onClick = { /* Navigate to specific feature */ }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = status, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}