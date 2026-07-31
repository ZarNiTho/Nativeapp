package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val repairs by viewModel.repairs.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Calculations
    val totalRevenue = remember(transactions) {
        transactions.filter { it.type == "ဝင်ငွေ" && it.group != "Personal" }.sumOf { it.amount }
    }
    val manualExpenses = remember(transactions) {
        transactions.filter { it.type == "ထွက်ငွေ" && it.group != "Personal" }.sumOf { it.amount }
    }
    val salesCOGS = remember(products) {
        products.filter { it.status == "ရောင်းပြီး" }.sumOf { it.costPrice }
    }
    val repairCOGS = remember(repairs) {
        repairs.filter { it.status.contains("ထုတ်ယူပြီး") && !it.status.contains("ပြင်မရ") }.sumOf { it.cost }
    }
    val totalCOGS = salesCOGS + repairCOGS
    val netProfit = totalRevenue - (manualExpenses + totalCOGS)
    val profitMarginPct = if (totalRevenue > 0) ((netProfit / totalRevenue) * 100).toInt() else 0

    // Personal Finance
    val personalInc = remember(transactions) {
        transactions.filter { it.type == "ဝင်ငွေ" && it.group == "Personal" }.sumOf { it.amount }
    }
    val personalExp = remember(transactions) {
        transactions.filter { it.type == "ထွက်ငွေ" && it.group == "Personal" }.sumOf { it.amount }
    }

    // Repair Stats
    val totalRepairs = repairs.size
    val successRepairs = repairs.count { it.status.contains("အောင်မြင်") || it.status == "ပြင်ပြီး-မရွေးသေး" }
    val failedRepairs = repairs.count { it.status.contains("ပြင်မရ") }
    val repairSuccessRate = if (successRepairs + failedRepairs > 0) {
        ((successRepairs.toDouble() / (successRepairs + failedRepairs)) * 100).toInt()
    } else 0

    // Low stock items
    val lowStockItems = remember(products) {
        products.filter { it.amount <= 1 && it.status != "ရောင်းပြီး" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Low Stock Banner Alert
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandRose.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = BrandRose)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "⚠️ လက်ကျန်နည်းနေသော ပစ္စည်း ${lowStockItems.size} မျိုး!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandRose
                            )
                            Text(
                                text = lowStockItems.take(3).joinToString(", ") { "${it.name} (${it.amount})" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // P&L Hero Card (Admin restricted)
        item {
            val isAdmin = currentUser.role.equals("admin", ignoreCase = true)

            if (isAdmin) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandLime),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "အသားတင်အမြတ် (Net Profit)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Margin: $profitMarginPct%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${netProfit.toInt()} Ks",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color.Black.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📈 စုစုပေါင်းဝင်ငွေ: +${totalRevenue.toInt()} Ks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("📉 ပစ္စည်းအရင်း: -${totalCOGS.toInt()} Ks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("🔒 အသားတင် အမြတ်နှင့် စုစုပေါင်း ငွေစာရင်းချုပ်", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Admin မန်နေဂျာ အကောင့်မှသာ ကြည့်ရှုခွင့် ရှိပါသည် (Staff Role)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Repair Performance Metric Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ပြင်ဆင် အောင်မြင်နှုန်း", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$repairSuccessRate%", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = BrandSky)
                        Text("စုစုပေါင်း $totalRepairs လုံး လက်ခံပြင်ဆင်ခဲ့သည်", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(text = "အောင်မြင်: $successRepairs လုံး", type = "success")
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(text = "ပြင်မရ: $failedRepairs လုံး", type = "danger")
                    }
                }
            }
        }

        // Personal Finance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💳 Personal Finance (ကိုယ်ပိုင်ငွေ)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandViolet)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ဝင်ငွေ: +${personalInc.toInt()} Ks", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text("ထွက်ငွေ: -${personalExp.toInt()} Ks", fontSize = 12.sp, color = BrandRose, fontWeight = FontWeight.Bold)
                        Text("လက်ကျန်: ${(personalInc - personalExp).toInt()} Ks", fontSize = 12.sp, color = BrandViolet, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Account Management
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("👥 Account & User Roles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(users) { user ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.username, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(user.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(text = user.role.uppercase(), type = if (user.role == "admin") "danger" else "info")
                        Spacer(modifier = Modifier.width(8.dp))
                        if (user.id != currentUser.id) {
                            TextButton(onClick = {
                                val newRole = if (user.role == "admin") "staff" else "admin"
                                viewModel.updateUserRole(user, newRole)
                            }) {
                                Text("Toggle Role", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Activity Logs
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("📝 System Activity Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(activityLogs.take(20)) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("👤 ${log.user}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandSky)
                        Text(
                            text = SimpleDateFormat("HH:mm:ss yyyy-MM-dd", Locale.getDefault()).format(Date(log.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(log.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
