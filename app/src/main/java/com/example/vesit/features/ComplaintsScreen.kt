package com.example.vesit.features.complaints

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Form State
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Infrastructure", "Canteen", "Academic", "Technical", "Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Raise a Ticket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Illustration/Icon
            Icon(
                Icons.Default.ReportProblem,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFD32F2F)
            )
            Text("How can we help?", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Admin will review your request shortly.", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Category Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                selectedCategory = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe the issue") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("Tell us more about the problem...") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            // Updated Submit Button Logic
            Button(
                onClick = {
                    if (description.isBlank()) {
                        Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                    // ANONYMOUS TICKET DATA
                    // Removed userId and userName for privacy
                    val ticket = hashMapOf(
                        "category" to selectedCategory,
                        "description" to description,
                        "status" to "Pending",
                        "date" to today,
                        "timestamp" to System.currentTimeMillis(),
                        "isAnonymous" to true // Flag for admin to know it's hidden
                    )

                    db.collection("complaints")
                        .add(ticket)
                        .addOnSuccessListener {
                            isSubmitting = false
                            Toast.makeText(context, "Anonymous Ticket Raised!", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            isSubmitting = false
                            Toast.makeText(context, "Failed to send: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Anonymously", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}