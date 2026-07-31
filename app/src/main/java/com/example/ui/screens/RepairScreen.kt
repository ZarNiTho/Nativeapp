package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RepairEntity
import com.example.ui.AppViewModel
import com.example.ui.components.CustomSearchBar
import com.example.ui.components.FilterPill
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandLime
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandRose
import com.example.ui.theme.BrandSky
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RepairScreen(viewModel: AppViewModel) {
    val repairs by viewModel.repairs.collectAsState()
    val searchQuery by viewModel.searchQueryRep.collectAsState()
    val filterStatus by viewModel.repStatusFilter.collectAsState()

    var showRepairModal by remember { mutableStateOf(false) }
    var editingRepair by remember { mutableStateOf<RepairEntity?>(null) }

    val statusCategories = listOf("အားလုံး", "စစ်ဆေးဆဲ", "ပြင်ပြီး", "ရွေးသွားပြီ")

    // Filter Logic
    val filteredRepairs = remember(repairs, searchQuery, filterStatus) {
        repairs.filter { r ->
            val matchesQuery = searchQuery.isBlank() ||
                    r.name.contains(searchQuery, ignoreCase = true) ||
                    r.vrno.toString().contains(searchQuery) ||
                    r.model.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "စစ်ဆေးဆဲ" -> r.status == "စစ်ဆေးပြုပြင်နေဆဲ" || r.status.contains("မရွေးသေး")
                "ပြင်ပြီး" -> r.status == "ပြင်ပြီး-မရွေးသေး"
                "ရွေးသွားပြီ" -> r.status.contains("ထုတ်ယူပြီး")
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Add Repair Button
            Button(
                onClick = {
                    editingRepair = null
                    showRepairModal = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSky)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ အပ်ဖုန်း အသစ်သွင်းရန်", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQueryRep.value = it },
                placeholder = "🔍 အမည် / VR နံပါတ်ဖြင့် ရှာရန်..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusCategories) { st ->
                    FilterPill(
                        label = st,
                        isSelected = filterStatus == st,
                        onClick = { viewModel.repStatusFilter.value = st }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Repairs List
            if (filteredRepairs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ပြင်ဆင်မှုမှတ်တမ်း မရှိသေးပါ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredRepairs, key = { it.id }) { repair ->
                        RepairItemCard(
                            repair = repair,
                            onClick = {
                                editingRepair = repair
                                showRepairModal = true
                            }
                        )
                    }
                }
            }
        }

        // Repair Modal Dialog
        if (showRepairModal) {
            RepairEditModal(
                repair = editingRepair,
                onDismiss = { showRepairModal = false },
                onSave = { savedRepair ->
                    viewModel.saveRepair(savedRepair)
                    showRepairModal = false
                },
                onGenerateVoucher = { r ->
                    viewModel.setVoucherPreview(r)
                    showRepairModal = false
                },
                onVoid = { rId ->
                    viewModel.voidRepair(rId)
                    showRepairModal = false
                }
            )
        }
    }
}

@Composable
fun RepairItemCard(
    repair: RepairEntity,
    onClick: () -> Unit
) {
    val isVoid = repair.status == "ပယ်ဖျက် (Void)"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVoid) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repair.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "VR: ${repair.vrno}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📍 ${repair.location.ifBlank { "မဖော်ပြပါ" }}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📞 ${repair.phone.ifBlank { "မဖော်ပြပါ" }}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📱 ${repair.model} | ⚠️ ${repair.issue}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandRose
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = repair.status,
                    type = when {
                        repair.status.contains("အောင်မြင်") -> "success"
                        repair.status.contains("မရ") || isVoid -> "danger"
                        repair.status.contains("ပြင်ပြီး") -> "info"
                        else -> "warning"
                    }
                )

                Text(
                    text = "📅 ${repair.date}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairEditModal(
    repair: RepairEntity?,
    onDismiss: () -> Unit,
    onSave: (RepairEntity) -> Unit,
    onGenerateVoucher: (RepairEntity) -> Unit,
    onVoid: (Long) -> Unit
) {
    var vrText by remember { mutableStateOf((repair?.vrno ?: 101).toString()) }
    var name by remember { mutableStateOf(repair?.name ?: "") }
    var phone by remember { mutableStateOf(repair?.phone ?: "") }
    var location by remember { mutableStateOf(repair?.location ?: "") }
    var model by remember { mutableStateOf(repair?.model ?: "") }
    var issue by remember { mutableStateOf(repair?.issue ?: "") }
    var remark by remember { mutableStateOf(repair?.remark ?: "") }
    var status by remember { mutableStateOf(repair?.status ?: "စစ်ဆေးပြုပြင်နေဆဲ") }
    var costText by remember { mutableStateOf((repair?.cost?.toInt() ?: 0).toString()) }
    var incomeText by remember { mutableStateOf((repair?.income?.toInt() ?: 0).toString()) }

    val statusOptions = listOf(
        "⏳ စစ်ဆေးပြုပြင်နေဆဲ",
        "✅ ပြင်ပြီး (မရွေးသေး)",
        "❌ ပြင်မရ (မရွေးသေး)",
        "📦 ပြင်ပြီး ရွေးသွားပြီ (Collected)",
        "📦 ပြင်မရ ရွေးသွားပြီ",
        "🚫 ပယ်ဖျက် (Void)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (repair == null) "ဖုန်းအသစ်အပ်ရန်" else "VR: ${repair.vrno} အသေးစိတ်",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vrText,
                            onValueChange = { vrText = it },
                            label = { Text("VR No *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Customer အမည် *") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("ဖုန်းနံပါတ်") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("နေရပ်လိပ်စာ") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("ဖုန်း Model *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = issue,
                        onValueChange = { issue = it },
                        label = { Text("ပြစ်ချက် (Issue) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("မှတ်ချက်") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (repair != null) {
                    item {
                        Divider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Status & Pricing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                    }

                    item {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                statusOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            status = when {
                                                opt.contains("စစ်ဆေးပြုပြင်နေဆဲ") -> "စစ်ဆေးပြုပြင်နေဆဲ"
                                                opt.contains("ပြင်ပြီး (မရွေးသေး)") -> "ပြင်ပြီး-မရွေးသေး"
                                                opt.contains("ပြင်မရ (မရွေးသေး)") -> "ပြင်မရ-မရွေးသေး"
                                                opt.contains("ပြင်ပြီး ရွေးသွားပြီ") -> "ထုတ်ယူပြီး-အောင်မြင်"
                                                opt.contains("ပြင်မရ ရွေးသွားပြီ") -> "ထုတ်ယူပြီး-ပြင်မရ"
                                                else -> "ပယ်ဖျက် (Void)"
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it },
                                label = { Text("အရင်း Ks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = incomeText,
                                onValueChange = { incomeText = it },
                                label = { Text("ရငွေ Ks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val vr = vrText.toLongOrNull() ?: 101L
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val updated = RepairEntity(
                        id = repair?.id ?: 0L,
                        vrno = vr,
                        date = repair?.date ?: todayStr,
                        name = name.ifBlank { "Unknown" },
                        phone = phone,
                        location = location,
                        model = model.ifBlank { "Phone" },
                        issue = issue.ifBlank { "Repair" },
                        remark = remark,
                        status = status,
                        cost = costText.toDoubleOrNull() ?: 0.0,
                        income = incomeText.toDoubleOrNull() ?: 0.0
                    )
                    onSave(updated)
                }
            ) {
                Text("သိမ်းမည်", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (repair != null) {
                    OutlinedButton(
                        onClick = { onGenerateVoucher(repair) }
                    ) {
                        Text("🧾 Receipt", fontSize = 11.sp)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("ပိတ်မည်")
                }
            }
        }
    )
}
