package com.example.vesit.features.exams

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Schedule", "Results")

    Scaffold(
        containerColor = Color(0xFFFBFBFF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exams", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null, modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Modern Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1A1C1E),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFC2185B)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    item { ExamCountdownCard() }
                    items(getExamSchedule()) { exam -> ExamScheduleRow(exam) }
                } else {
                    item { SGPAStatsCard() }
                    items(getResultData()) { result -> ResultRow(result) }
                }
            }
        }
    }
}

@Composable
fun ExamCountdownCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Next Exam In", fontSize = 14.sp, color = Color(0xFFC2185B))
            Text("04 Days : 22 Hrs", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF880E4F))
            Text("Internal Assessment - Java Full Stack", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ExamScheduleRow(exam: ExamInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(exam.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${exam.date} • ${exam.time}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(exam.room, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
        }
    }
}

@Composable
fun SGPAStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Current CGPA", color = Color.White.copy(0.7f), fontSize = 14.sp)
                Text("9.12", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun ResultRow(res: ResultInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(res.subject, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Grade: ${res.grade}", fontSize = 12.sp, color = Color.Gray)
        }
        Text(res.pointer.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1A1C1E))
    }
}

// Data Classes & Mocks
data class ExamInfo(val subject: String, val date: String, val time: String, val room: String)
data class ResultInfo(val subject: String, val grade: String, val pointer: Double)

fun getExamSchedule() = listOf(
    ExamInfo("Java Full Stack", "05 Feb", "10:30 AM", "Lab 402"),
    ExamInfo("Software Engineering", "07 Feb", "02:00 PM", "CR 301"),
    ExamInfo("Data Structures", "10 Feb", "10:30 AM", "CR 301")
)

fun getResultData() = listOf(
    ResultInfo("Advanced Programming", "O", 10.0),
    ResultInfo("Discrete Mathematics", "A+", 9.0),
    ResultInfo("Computer Networks", "A", 8.5)
)