package com.example.vesit.features.attendance

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vesit.features.attendance.data.BeaconScanner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun markAttendance(context: Context, selectedSubject: String) {
        val user = auth.currentUser ?: return
        val beaconScanner = BeaconScanner(context)

        viewModelScope.launch {
            _isLoading.value = true
            _attendanceStatus.value = "Searching for Teacher..."

            try {
                beaconScanner.startScanning()

                // Proximity Handshake Loop
                var timeoutCounter = 0
                val maxTimeout = 100
                while (!beaconScanner.isTeacherNearby.value && timeoutCounter < maxTimeout) {
                    delay(100)
                    timeoutCounter++
                }
                beaconScanner.stopScanning()

                // In markAttendance function, update the Success block:
                if (beaconScanner.isTeacherNearby.value) {
                    saveAttendanceToFirestore(context, selectedSubject)
                    _attendanceStatus.value = "Success: Attendance Marked!"

                    // Direct UI feedback
                    android.widget.Toast.makeText(context, "Attendance Sent to Teacher!", android.widget.Toast.LENGTH_SHORT).show()
                }else {
                    _attendanceStatus.value = "Error: Teacher signal not detected."
                }

            } catch (e: Exception) {
                _attendanceStatus.value = "Failed: ${e.message}"
            } finally {
                _isLoading.value = false
                beaconScanner.stopScanning()
            }
        }
    }

    private suspend fun saveAttendanceToFirestore(context: Context, subject: String) {
        val auth = FirebaseAuth.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Check if we are in real mode or demo mode
        val email = auth.currentUser?.email ?: "rahul.student@ves.ac.in"
        val uid = auth.currentUser?.uid ?: "demo_rahul_123"

        val broadcastData = hashMapOf(
            "email" to email,
            "date" to today,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Using the FLAT live_sync collection that worked for you
        db.collection("live_sync")
            .document("${uid}_$today")
            .set(broadcastData)
            .await()
    }
}