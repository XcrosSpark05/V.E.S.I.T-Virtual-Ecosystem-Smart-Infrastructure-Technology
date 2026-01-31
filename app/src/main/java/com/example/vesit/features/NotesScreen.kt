package com.example.vesit.features.notes

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navController: NavController) {
    val categories = listOf("All", "Core", "Labs", "Question Papers")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Color(0xFFFBFBFF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Study Materials", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null, modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Category Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1A1C1E),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Notes List
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                val filteredNotes = getSampleNotes().filter {
                    selectedCategory == "All" || it.type == selectedCategory
                }

                items(filteredNotes) { note ->
                    NoteResourceCard(note)
                }
            }
        }
    }
}

@Composable
fun NoteResourceCard(note: NoteResource) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(note.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, null, tint = note.color)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                Text("${note.subject} • ${note.fileSize}", fontSize = 12.sp, color = Color.Gray)
            }

            IconButton(onClick = { /* Handle Download/View */ }) {
                Icon(Icons.Default.FileDownload, null, tint = Color.Gray)
            }
        }
    }
}

data class NoteResource(
    val title: String,
    val subject: String,
    val type: String,
    val fileSize: String,
    val color: Color
)

fun getSampleNotes() = listOf(
    NoteResource("Spring Boot Basics.pdf", "Java Full Stack", "Core", "2.4 MB", Color(0xFF388E3C)),
    NoteResource("DSA Lab Manual.pdf", "Data Structures", "Labs", "4.1 MB", Color(0xFF1976D2)),
    NoteResource("Software Models.ppt", "Software Eng.", "Core", "1.8 MB", Color(0xFFF57C00)),
    NoteResource("Unit 1 QB.pdf", "Maths", "Question Papers", "850 KB", Color(0xFFD32F2F)),
    NoteResource("RSA Algorithm.pdf", "Cryptography", "Core", "1.2 MB", Color(0xFF7B1FA2))
)