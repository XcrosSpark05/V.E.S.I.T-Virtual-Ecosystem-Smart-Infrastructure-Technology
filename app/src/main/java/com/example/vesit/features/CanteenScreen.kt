package com.example.vesit.features.canteen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun CanteenScreen(navController: NavController) {
    val categories = listOf("All", "Snacks", "Meals", "Drinks")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Color(0xFFFBFBFF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("VESIT Canteen", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null, modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Category Selector
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

            // Menu Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                val menu = getCanteenMenu().filter {
                    selectedCategory == "All" || it.category == selectedCategory
                }

                items(menu) { item ->
                    FoodItemCard(item)
                }
            }
        }
    }
}

@Composable
fun FoodItemCard(item: MenuItem) {
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
            // Placeholder for Food Image / Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3E5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(item.category) {
                        "Drinks" -> Icons.Default.LocalDrink
                        "Meals" -> Icons.Default.Restaurant
                        else -> Icons.Default.Fastfood
                    },
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                Text(item.category, fontSize = 12.sp, color = Color.Gray)
            }

            Text(
                "₹${item.price}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

data class MenuItem(val name: String, val price: Int, val category: String)

fun getCanteenMenu() = listOf(
    MenuItem("Samosa Pav", 20, "Snacks"),
    MenuItem("Misal Pav", 45, "Snacks"),
    MenuItem("Veg Thali", 80, "Meals"),
    MenuItem("Paneer Biryani", 110, "Meals"),
    MenuItem("Cold Coffee", 30, "Drinks"),
    MenuItem("Tea / Chai", 10, "Drinks"),
    MenuItem("Meda Vada", 40, "Snacks"),
    MenuItem("Idli Sambhar", 35, "Snacks")
)