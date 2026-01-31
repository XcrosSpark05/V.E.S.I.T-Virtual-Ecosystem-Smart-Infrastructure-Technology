package com.example.vesit.features.auth

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vesit.features.auth.data.GoogleAuthUiHelper

@Composable
fun LoginScreen(viewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    val authHelper = remember { GoogleAuthUiHelper(context) }
    val user by viewModel.userState.collectAsState()

    // MI Phone Detection
    val manufacturer = Build.MANUFACTURER.lowercase()
    val isMiPhone = manufacturer.contains("xiaomi") || manufacturer.contains("poco") || manufacturer.contains("redmi")

    LaunchedEffect(user, viewModel.isDemoMode) {
        if (viewModel.isDemoMode) {
            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
        } else {
            user?.let { firebaseUser ->
                val email = firebaseUser.email ?: ""
                if (email.endsWith("@ves.ac.in")) {
                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                } else if (email.endsWith("@gmail.com")) {
                    navController.navigate("teacher_home") { popUpTo("login") { inclusive = true } }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // HIDDEN GESTURE ON BRANDING
        Text(
            text = "VESIT NEXUS",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    if (isMiPhone) {
                        Toast.makeText(context, "Entering Demo Mode: Rahul", Toast.LENGTH_SHORT).show()
                        viewModel.bypassLoginForDemo()
                    }
                })
            }
        )
        Text(text = "Smart Attendance Ecosystem", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(60.dp))

        LoginPortalCard(
            title = "Student Login",
            subtitle = "Access your attendance",
            icon = Icons.Default.School,
            onClick = { viewModel.signIn(authHelper) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginPortalCard(
            title = "Staff Login",
            subtitle = "Manage classes",
            icon = Icons.Default.SupervisorAccount,
            onClick = { viewModel.signIn(authHelper) }
        )

        if (isMiPhone) {
            Text(
                "MI Optimization detected. Long-press title to bypass.",
                fontSize = 10.sp, color = Color.LightGray,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}
@Composable
fun LoginPortalCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}