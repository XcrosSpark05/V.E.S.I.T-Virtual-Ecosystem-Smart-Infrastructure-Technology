package com.example.vesit.features.schedule

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(navController: NavController) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    var selectedDay by remember { mutableStateOf("Mon") }

    Scaffold(
        containerColor = Color(0xFFFBFBFF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Weekly Schedule", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null, modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // DAY SELECTOR (Horizontal Scroll)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                days.forEach { day ->
                    val isSelected = selectedDay == day
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDay = day },
                        label = { Text(day, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1A1C1E),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // LECTURE LIST
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(getScheduleForDay(selectedDay)) { lecture ->
                    LectureItem(lecture)
                }
            }
        }
    }
}

@Composable
fun LectureItem(lecture: LectureData) {
    val currentTime = LocalTime.now()
    val isOngoing = currentTime.isAfter(lecture.startTime) && currentTime.isBefore(lecture.endTime)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // Time Column
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(lecture.startTime.format(DateTimeFormatter.ofPattern("HH:mm")), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Box(modifier = Modifier.width(2.dp).height(60.dp).background(if (isOngoing) Color(0xFF6200EE) else Color.LightGray))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Subject Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOngoing) Color(0xFFF3E5F5) else Color.White
            ),
            border = if (isOngoing) BorderStroke(1.dp, Color(0xFF6200EE)) else BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(lecture.subject, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                    Text(lecture.room, fontSize = 12.sp, color = Color.Gray)
                }
                if (isOngoing) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Ongoing", tint = Color(0xFF6200EE))
                }
            }
        }
    }
}

data class LectureData(
    val subject: String,
    val room: String,
    val startTime: LocalTime,
    val endTime: LocalTime
)

fun getScheduleForDay(day: String): List<LectureData> = when (day) {
    "Mon" -> listOf(
        LectureData("Java Full Stack", "Lab 402", LocalTime.of(8, 30), LocalTime.of(10, 30)),
        LectureData("Software Engineering", "CR 301", LocalTime.of(10, 45), LocalTime.of(12, 45)),
        LectureData("Lunch Break", "Canteen", LocalTime.of(12, 45), LocalTime.of(13, 30)),
        LectureData("Data Structures", "CR 301", LocalTime.of(13, 30), LocalTime.of(15, 30))
    )
    // Add other days as needed
    else -> listOf(LectureData("MCA Lectures", "VESIT Campus", LocalTime.of(8, 30), LocalTime.of(15, 30)))
}