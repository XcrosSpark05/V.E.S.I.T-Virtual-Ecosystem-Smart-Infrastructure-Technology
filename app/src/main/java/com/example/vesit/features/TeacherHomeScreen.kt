package com.example.vesit.features.teacher

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vesit.features.teacher.data.BeaconBroadcaster
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    val broadcaster = remember { BeaconBroadcaster() }

    var isBeaming by remember { mutableStateOf(false) }
    var presentStudents by remember { mutableStateOf(listOf<String>()) }
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // --- FIXED: Create the AdvertiseCallback object here ---
    val advertiseCallback = remember {
        object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                isBeaming = true
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                isBeaming = false
            }
        }
    }

    if (currentUser == null) {
        LaunchedEffect(Unit) { navController.navigate("login") { popUpTo(0) { inclusive = true } } }
        return
    }

    LaunchedEffect(isBeaming) {
        if (isBeaming) {
            val listener = db.collection("live_sync")
                .whereEqualTo("date", todayDate)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    val emails = snapshots?.documents?.mapNotNull { it.getString("email") } ?: emptyList()
                    presentStudents = emails.distinct()
                }
        } else {
            presentStudents = emptyList()
        }
    }

    Scaffold(containerColor = Color(0xFFFBFBFF)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Professor Dashboard", fontSize = 14.sp, color = Color.Gray)
                    Text(currentUser.displayName ?: "Faculty", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
                IconButton(onClick = { auth.signOut(); navController.navigate("login") { popUpTo(0) { inclusive = true } } }) {
                    Icon(Icons.Default.Logout, null, tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            RadarCard(
                isBeaming = isBeaming,
                onToggle = @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE) {
                    if (!isBeaming) {
                        // PASS THE CALLBACK OBJECT INSTEAD OF A LAMBDA
                        broadcaster.startBroadcasting(advertiseCallback)
                    } else {
                        broadcaster.stopBroadcasting(advertiseCallback)
                        isBeaming = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Detected Students (${presentStudents.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (presentStudents.isEmpty()) {
                    item { Text("Waiting for student handshakes...", color = Color.Gray, modifier = Modifier.padding(10.dp)) }
                }
                items(presentStudents) { email ->
                    StudentEntryRow(email)
                }
            }
        }
    }
}

@Composable
fun RadarCard(isBeaming: Boolean, onToggle: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radius by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 300f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)), label = "wave"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)), label = "fade"
    )

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                if (isBeaming) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(color = Color(0xFF1976D2), radius = radius, alpha = alpha, style = Stroke(width = 6.dp.toPx()))
                    }
                }
                Surface(
                    modifier = Modifier.size(100.dp).clickable { onToggle() },
                    shape = CircleShape,
                    color = if (isBeaming) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                    border = BorderStroke(2.dp, if (isBeaming) Color(0xFF1976D2) else Color.LightGray)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = if (isBeaming) Icons.Default.WifiTethering else Icons.Default.Radar,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (isBeaming) Color(0xFF1976D2) else Color.Gray
                        )
                        Text(if (isBeaming) "LIVE" else "START", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = if (isBeaming) "Proximity Shield Active" else "System Offline",
                color = if (isBeaming) Color(0xFF1976D2) else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StudentEntryRow(email: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF2E7D32))
            Spacer(Modifier.width(12.dp))
            Text(email, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
        }
    }
}