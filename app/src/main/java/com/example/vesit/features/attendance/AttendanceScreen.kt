package com.example.vesit.features.attendance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vesit.ui.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// --- THEME COLORS ---
val SoftPurple = Color(0xFFF3E5F5)
val SoftGreen = Color(0xFFE8F5E9)
val SoftOrange = Color(0xFFFFF3E0)
val TextDark = Color(0xFF1A1C1E)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(navController: NavController) {
    val viewModel: AttendanceViewModel = viewModel()
    val context = LocalContext.current
    val status by viewModel.attendanceStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM"))
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val scanPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            showSheet = true
            viewModel.markAttendance(context, "Android Dev")
        } else {
            Toast.makeText(context, "Permissions required for Proximity Shield!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFFBFBFF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Attendance", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back", tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
                    } else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

                    if (perms.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
                        showSheet = true
                        viewModel.markAttendance(context, "Android Dev")
                    } else {
                        scanPermissionLauncher.launch(perms)
                    }
                },
                containerColor = TextDark,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.WifiTethering, null)
                Spacer(Modifier.width(8.dp))
                Text("Join Live Class", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = today, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "History & Insights", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                }
            }

            item { ModernCalendarCard() }

            item {
                Text("Presence Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(12.dp))
                ModernStatsCard(total = 40, present = 34)
            }

            item {
                Text("Subject Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }

            items(getSampleSubjects()) { subject ->
                ModernSubjectCard(subject)
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { if (!isLoading) showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                HandshakeStatusContent(
                    status = status ?: "Initializing...",
                    isLoading = isLoading,
                    onClose = { showSheet = false }
                )
            }
        }
    }
}

@Composable
fun HandshakeStatusContent(status: String, isLoading: Boolean, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp, start = 30.dp, end = 30.dp, top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val rotate by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)), label = "rotate"
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotate },
                    color = TextDark,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
            }

            when {
                isLoading -> Icon(Icons.Default.Radar, null, tint = TextDark, modifier = Modifier.size(40.dp))
                status.contains("Success") -> Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF34D399), modifier = Modifier.size(80.dp))
                else -> Icon(Icons.Default.Warning, null, tint = Color(0xFFF87171), modifier = Modifier.size(80.dp))
            }
        }

        Text(
            text = if (isLoading) "Establishing Handshake..." else status,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = TextDark
        )

        if (!isLoading && !status.contains("Success")) {
            Text(
                text = "The Proximity Shield requires you to be near the professor. Please move closer and ensure the class is 'Live'.",
                fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 20.sp
            )
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextDark)
        ) {
            Text(if (status.contains("Success")) "Finish" else "Try Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        modifier = Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFFE1BEE7)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SoftPurple.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$monthName ${currentMonth.year}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, null, tint = TextDark)
                    }
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = TextDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(text = day, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            for (i in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (j in 0 until 7) {
                        val dayNum = i * 7 + j - firstDayOfMonth + 1
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (dayNum in 1..daysInMonth) {
                                val isPresent = presentDates.contains(currentMonth.atDay(dayNum))
                                CalendarDay(dayNum.toString(), isPresent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(day: String, isPresent: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isPresent) TextDark else Color.Transparent,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day, fontSize = 14.sp,
                fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Medium,
                color = if (isPresent) Color.White else TextDark
            )
        }
    }
}

@Composable
fun ModernStatsCard(total: Int, present: Int) {
    val percentage = (present.toFloat() / total)
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing), label = "stat"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawCircle(color = Color(0xFFF5F5F5), style = Stroke(width = 18f))
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color(0xFF6200EE), Color(0xFFBB86FC))),
                        startAngle = -90f, sweepAngle = 360 * animatedProgress, useCenter = false,
                        style = Stroke(width = 18f, cap = StrokeCap.Round)
                    )
                }
                Text("${(animatedProgress * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text("Presence Score", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text("$present / $total Lectures", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFF6200EE), trackColor = Color(0xFFF5F5F5)
                )
            }
        }
    }
}

@Composable
fun ModernSubjectCard(subject: SubjectData) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F9FA)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MenuBook, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(subject.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text("${subject.present}/${subject.total} Lectures", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Text("${subject.percent}%", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if(subject.percent < 75) Color(0xFFD32F2F) else Color(0xFF388E3C))
        }
    }
}

data class SubjectData(val name: String, val total: Int, val present: Int, val percent: Int)

fun getSampleSubjects() = listOf(
    SubjectData("Java Full Stack", 12, 10, 83),
    SubjectData("Android Dev", 10, 9, 90),
    SubjectData("Software Engineering", 8, 5, 62),
    SubjectData("Data Structures", 15, 12, 80)
)