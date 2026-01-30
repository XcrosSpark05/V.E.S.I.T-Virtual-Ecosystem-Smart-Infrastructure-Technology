package com.example.vesit.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vesit.features.auth.data.GoogleAuthUiHelper

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val authHelper = remember { GoogleAuthUiHelper(context) }
    val user by viewModel.userState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    // Automatic Navigation: Triggered when user state changes to non-null
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("dashboard") {
                // Pop up to "login" to remove it from the backstack
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "V.E.S.I.T",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Virtual Ecosystem & Smart Infrastructure Technology",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Trigger Google Sign-In via Credential Manager
        Button(
            onClick = { viewModel.signIn(authHelper) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Login with VESIT Email",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Note: Only @ves.ac.in accounts are authorized.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}