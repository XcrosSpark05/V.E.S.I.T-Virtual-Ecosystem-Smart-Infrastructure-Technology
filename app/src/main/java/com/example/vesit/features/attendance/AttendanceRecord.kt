package com.example.vesit.features.attendance.data

import com.google.firebase.Timestamp

data class AttendanceRecord(
    val status: String = "Present",
    val timestamp: Timestamp = Timestamp.now(),
    val deviceId: String = ""
)