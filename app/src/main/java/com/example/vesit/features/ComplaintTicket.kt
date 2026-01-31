package com.example.vesit.features

data class ComplaintTicket(
    val userId: String = "",
    val userName: String = "",
    val category: String = "",
    val description: String = "",
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = ""
)
