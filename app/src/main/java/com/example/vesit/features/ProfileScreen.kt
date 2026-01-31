package com.example.vesit.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFF3E5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = Color(0xFF7B1FA2))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Prathamesh Karbele", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(user?.email ?: "student@ves.ac.in", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(Modifier.padding(20.dp)) {
                    ProfileDetailItem(Icons.Default.School, "Department", "MCA - 1st Year")
                    Divider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                    ProfileDetailItem(Icons.Default.LocationOn, "College", "VESIT")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Updated Button in ProfileScreen.kt
            Button(
                onClick = {
                    // 1. Sign out from Firebase
                    FirebaseAuth.getInstance().signOut()

                    // 2. FORCE navigate to login and wipe everything
                    navController.navigate("login") {
                        // This is the strongest way to clear the stack in Compose
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}