package com.example.vesit.features.share

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

// --- RE-OPTIMIZED NEARBY MANAGER ---
class NexusBridge(private val context: Context) {
    private val client = Nearby.getConnectionsClient(context)
    private val SERVICE_ID = "com.example.vesit.OFFLINE_SYNC"

    // Strategy: P2P_STAR is usually better for 1-to-many (Teacher to Students)
    private val STRATEGY = Strategy.P2P_STAR

    fun startAdvertising(onResult: (String, Any) -> Unit) {
        client.stopAdvertising()
        // FIX: Explicitly disable NFC to stop the Logcat error and speed up discovery
        val options = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        client.startAdvertising("Nexus_Receiver", SERVICE_ID, object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
                client.acceptConnection(id, createPayloadCallback(onResult))
            }
            override fun onConnectionResult(id: String, res: ConnectionResolution) {
                if (res.status.isSuccess) onResult("STATUS", "Handshake Success!")
            }
            override fun onDisconnected(id: String) { onResult("STATUS", "Disconnected") }
        }, options)
    }

    fun startDiscovery(onConnected: (String) -> Unit) {
        client.stopDiscovery()
        // FIX: Explicitly disable NFC here as well
        val options = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        client.startDiscovery(SERVICE_ID, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
                client.requestConnection("Nexus_Sender", id, object : ConnectionLifecycleCallback() {
                    override fun onConnectionInitiated(sid: String, info: ConnectionInfo) {
                        client.acceptConnection(sid, createPayloadCallback { _, _ -> })
                    }
                    override fun onConnectionResult(sid: String, res: ConnectionResolution) {
                        if (res.status.isSuccess) onConnected(sid)
                    }
                    override fun onDisconnected(id: String) {}
                })
            }
            override fun onEndpointLost(id: String) {}
        }, options)
    }

    private fun createPayloadCallback(onResult: (String, Any) -> Unit) = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                onResult("TEXT", String(payload.asBytes()!!))
            } else if (payload.type == Payload.Type.FILE) {
                // 1. The file is currently in a temp folder. We need to move it to make it visible.
                val receivedFile = payload.asFile()?.asJavaFile()

                receivedFile?.let { file ->
                    // 2. Trigger the "Gallery Scan"
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        arrayOf(file.absolutePath),
                        arrayOf("image/jpeg"), // or null for auto-detect
                        { path, uri ->
                            // 3. Update the UI once the gallery "sees" it
                            onResult("STATUS", "Gallery Synced! ✅")
                            android.util.Log.d("NexusBridge", "Scanned: $path, Uri: $uri")
                        }
                    )
                    onResult("FILE", "New Material Received!")
                }
            }
        }

        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                // Optional: Vibrate to alert the user
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    fun sendText(id: String, text: String) = client.sendPayload(id, Payload.fromBytes(text.toByteArray()))

    fun sendFile(id: String, uri: Uri) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.let {
                client.sendPayload(id, Payload.fromFile(it))
            }
        } catch (e: Exception) {
            Log.e("NexusBridge", "File send failed", e)
        }
    }

    fun disconnect() = client.stopAllEndpoints()
}

// --- UPDATED UI SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineShareScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val bridge = remember { NexusBridge(context) }
    var connectedId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("System Ready") }
    var receivedData by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            if (connectedId != null) {
                bridge.sendFile(connectedId!!, it)
                status = "Transferring..."
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Nexus Sync Engine", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E))

        Spacer(Modifier.height(24.dp))

        // Large Status Card with Visual Feedback
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = if(connectedId != null) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
            )
        ) {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if(connectedId != null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if(connectedId != null) Color(0xFF2E7D32) else Color.Gray
                )
                Text(status, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (receivedData.isNotEmpty()) {
                    Text(receivedData, modifier = Modifier.padding(top = 8.dp), color = Color(0xFF1B5E20))
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        if (connectedId == null) {
            // STEP 1: ONE PHONE HITS RECEIVE
            Button(
                onClick = {
                    status = "Broadcasting... (Receiver)"
                    bridge.startAdvertising { type, data ->
                        if (type == "TEXT") receivedData = "Msg: $data"
                        if (type == "STATUS") status = data as String
                        if (type == "FILE") status = "Material Received!"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("MODE: RECEIVE")
            }

            Spacer(Modifier.height(16.dp))

            // STEP 2: OTHER PHONE HITS SEND
            OutlinedButton(
                onClick = {
                    status = "Searching for friends..."
                    bridge.startDiscovery { id ->
                        connectedId = id
                        status = "Connected to Rahul!"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Wifi, null)
                Spacer(Modifier.width(8.dp))
                Text("MODE: SEND")
            }
        } else {
            // STEP 3: ONCE CONNECTED, SELECT CONTENT
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { picker.launch("*/*") },
                    Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SHARE FILE")
                }

                Button(
                    onClick = {
                        bridge.sendText(connectedId!!, "Handshake verified.")
                        status = "Syncing Data..."
                    },
                    Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("SYNC TEXT")
                }
            }

            Spacer(Modifier.height(40.dp))

            TextButton(onClick = {
                bridge.disconnect()
                connectedId = null
                status = "Ready"
                receivedData = ""
            }) {
                Text("RESET ENGINE", color = Color.Red)
            }
        }
    }
}