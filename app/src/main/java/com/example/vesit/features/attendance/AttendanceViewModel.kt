package com.example.vesit.features.attendance

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vesit.features.attendance.data.AttendanceRecord
import com.example.vesit.features.attendance.data.BeaconScanner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _attendanceStatus = MutableStateFlow<String?>(null)
    val attendanceStatus = _attendanceStatus.asStateFlow()

    // Loading state to show a spinner on the Dashboard card
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun markAttendanceWithProximity(context: Context) {
        val user = auth.currentUser ?: return
        val beaconScanner = BeaconScanner(context)
        val currentDeviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            _isLoading.value = true
            _attendanceStatus.value = "Scanning for Teacher's signal..."

            try {
                // 1. Start BLE Scanning
                beaconScanner.startScanning()

                // 2. Wait for a signal (Timeout after 7 seconds)
                var timeoutCounter = 0
                while (!beaconScanner.isTeacherNearby.value && timeoutCounter < 70) {
                    delay(100) // Check every 100ms
                    timeoutCounter++
                }
                beaconScanner.stopScanning()

                // 3. Proximity Check (The Shield)
                if (!beaconScanner.isTeacherNearby.value) {
                    _attendanceStatus.value = "Error: Teacher not detected. Are you in class?"
                    _isLoading.value = false
                    return@launch
                }

                // 4. Device Binding & Match Check
                val userDoc = db.collection("users").document(user.uid).get().await()
                if (!userDoc.exists() || !userDoc.contains("device_id")) {
                    db.collection("users").document(user.uid)
                        .set(mapOf("device_id" to currentDeviceId, "email" to user.email)).await()
                }

                val registeredDeviceId = userDoc.getString("device_id") ?: currentDeviceId
                if (registeredDeviceId != currentDeviceId) {
                    _attendanceStatus.value = "Error: Unauthorized Device!"
                    _isLoading.value = false
                    return@launch
                }

                // 5. Date Check & Final Write
                val attendanceDoc = db.collection("users").document(user.uid)
                    .collection("attendance").document(today).get().await()

                if (attendanceDoc.exists()) {
                    _attendanceStatus.value = "Already marked for today!"
                } else {
                    val record = AttendanceRecord(deviceId = currentDeviceId)
                    db.collection("users").document(user.uid)
                        .collection("attendance").document(today).set(record).await()
                    _attendanceStatus.value = "Attendance marked successfully!"
                }

            } catch (e: Exception) {
                _attendanceStatus.value = "Failed: ${e.message}"
            } finally {
                _isLoading.value = false
                beaconScanner.stopScanning()
            }
        }
    }
}