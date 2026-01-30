package com.example.vesit.features.attendance

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vesit.ui.* // Import your pastel colors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vesit.ui.SoftPurple
import com.example.vesit.ui.TextDark
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(navController: NavController) {
    val viewModel: AttendanceViewModel = viewModel()
    val context = LocalContext.current
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("Attendance History", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                }
            )
        },
        floatingActionButton = {
            // Floating Button matching the modern theme
            FloatingActionButton(
                onClick = { viewModel.markAttendanceWithProximity(context) },
                containerColor = TextDark,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Mark Attendance", tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Current Date Section
            item {
                Text(
                    text = "Today is $today",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 2. The Modern Calendar Card with Wave Pattern
            item {
                ModernCalendarCard()
            }

            // 3. Stats Section
            item {
                Text("Your Progress", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            }

            item {
                ModernStatsCard(total = 40, present = 34)
            }

            // 4. Subject Wise Breakdown
            item {
                Text("Subject Breakdown", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            }

            items(getSampleSubjects()) { subject ->
                ModernSubjectCard(subject)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun CalendarDay(day: String, isPresent: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isPresent) TextDark else Color.Transparent,
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day,
                fontSize = 14.sp,
                fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Medium,
                color = if (isPresent) Color.White else TextDark
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModernCalendarCard(presentDates: List<LocalDate> = listOf(LocalDate.now())) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SoftPurple),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    cubicTo(size.width * 0.3f, size.height * 0.7f, size.width * 0.7f, size.height * 0.95f, size.width, size.height * 0.85f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color.Black.copy(alpha = 0.05f))
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$monthName ${currentMonth.year}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Row {
                        // Corrected Icons to standard library equivalents
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.Default.KeyboardArrowLeft, "Last Month", tint = TextDark)
                        }
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.Default.KeyboardArrowRight, "Next Month", tint = TextDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    days.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                for (i in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (j in 0 until 7) {
                            val dayNum = i * 7 + j - firstDayOfMonth + 1
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (dayNum in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNum)
                                    val isPresent = presentDates.contains(date)
                                    CalendarDay(day = dayNum.toString(), isPresent = isPresent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ModernStatsCard(total: Int, present: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SoftOrange),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { present.toFloat() / total },
                    modifier = Modifier.size(70.dp),
                    strokeWidth = 8.dp,
                    color = TextDark,
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
                Text("${(present.toFloat()/total*100).toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text("Overall", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("$present out of $total lectures", fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun ModernSubjectCard(subject: SubjectData) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(subject.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextDark)
                Text("${subject.present}/${subject.total} Lectures", fontSize = 13.sp, color = Color.DarkGray)
            }
            Text(
                "${subject.percent}%",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if(subject.percent < 75) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )
        }
    }
}

// Add this data class and function at the very bottom of the file

data class SubjectData(
    val name: String,
    val total: Int,
    val present: Int,
    val percent: Int
)

fun getSampleSubjects() = listOf(
    SubjectData("Java Full Stack", 12, 10, 83),
    SubjectData("Android Dev", 10, 9, 90),
    SubjectData("Software Engineering", 8, 5, 62),
    SubjectData("Data Structures", 15, 12, 80)
)