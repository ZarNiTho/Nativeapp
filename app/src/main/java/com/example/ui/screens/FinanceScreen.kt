package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
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
import com.example.data.local.TransactionEntity
import com.example.ui.AppViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandLime
import com.example.ui.theme.BrandRose
import com.example.ui.theme.BrandSky
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinanceScreen(viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val products by viewModel.products.collectAsState()
    val repairs by viewModel.repairs.collectAsState()
    val monthFilter by viewModel.selectedFinanceMonth.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val isAdmin = currentUser.role.equals("admin", ignoreCase = true)

    var showFinModal by remember { mutableStateOf(false) }
    var modalType by remember { mutableStateOf("ဝင်ငွေ") }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Aggregate Metrics
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "ဝင်ငွေ" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "ထွက်ငွေ" }.sumOf { it.amount }
    }
    val cashOnHand = remember(totalIncome, totalExpense) {
        totalIncome - totalExpense
    }

    val investedCapital = remember(products, repairs) {
        val unsoldStockCost = products.filter { it.status != "ရောင်းပြီး" }.sumOf { it.amount * it.costPrice }
        val pendingRepairsCost = repairs.filter { !it.status.contains("ထုတ်ယူပြီး") && it.status != "ပယ်ဖျက် (Void)" }.sumOf { it.cost }
        unsoldStockCost + pendingRepairsCost
    }

    // Daily Grouping
    val groupedByDate = remember(transactions, monthFilter) {
        transactions
            .filter { it.date.startsWith(monthFilter) }
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Selector
            OutlinedTextField(
                value = monthFilter,
                onValueChange = { viewModel.selectedFinanceMonth.value = it },
                label = { Text("လအလိုက် စစ်ဆေးရန် (YYYY-MM)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Cards: Cash on Hand & Invested Capital (Admin restricted)
            if (isAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BrandSky.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("💰 လက်ရှိ အံဆွဲထဲမှငွေ (Admin)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandSky)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${cashOnHand.toInt()} Ks", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandSky)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📦 မြုပ်နေသော အရင်း (Admin)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${investedCapital.toInt()} Ks", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandGold)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "စုစုပေါင်း ငွေစာရင်းချုပ်နှင့် အံဆွဲထဲမှငွေ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Admin မန်နေဂျာ သာ ကြည့်ရှုခွင့် ရှိပါသည် (Staff Role)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        editingTransaction = null
                        modalType = "ဝင်ငွေ"
                        showFinModal = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ ဝင်ငွေ", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        editingTransaction = null
                        modalType = "ထွက်ငွေ"
                        showFinModal = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("- ထွက်ငွေ", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Transactions List
            if (groupedByDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ရွေးချယ်ထားသောလအတွက် ငွေစာရင်း မရှိသေးပါ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    groupedByDate.forEach { (dateStr, itemsList) ->
                        val dayInc = itemsList.filter { it.type == "ဝင်ငွေ" }.sumOf { it.amount }
                        val dayExp = itemsList.filter { it.type == "ထွက်ငွေ" }.sumOf { it.amount }
                        val dayNet = dayInc - dayExp

                        item {
                            DailyFinanceCard(
                                dateStr = dateStr,
                                dayInc = dayInc,
                                dayExp = dayExp,
                                dayNet = dayNet,
                                isAdmin = isAdmin,
                                transactions = itemsList,
                                onEditTransaction = { tx ->
                                    editingTransaction = tx
                                    modalType = tx.type
                                    showFinModal = true
                                },
                                onDeleteTransaction = { tx ->
                                    viewModel.deleteTransaction(tx)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Finance Add/Edit Modal
        if (showFinModal) {
            FinanceEditModal(
                transaction = editingTransaction,
                initialType = modalType,
                onDismiss = { showFinModal = false },
                onSave = { savedTx ->
                    viewModel.saveTransaction(savedTx)
                    showFinModal = false
                }
            )
        }
    }
}

@Composable
fun DailyFinanceCard(
    dateStr: String,
    dayInc: Double,
    dayExp: Double,
    dayNet: Double,
    isAdmin: Boolean,
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 $dateStr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isAdmin) "${if (dayNet >= 0) "+" else ""}${dayNet.toInt()} Ks" else "🔒 Admin သာ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (!isAdmin) MaterialTheme.colorScheme.onSurfaceVariant else if (dayNet >= 0) Color(0xFF2E7D32) else BrandRose
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isAdmin) "ဝင်: +${dayInc.toInt()} Ks" else "ဝင်ငွေ: 🔒 Restricted",
                    fontSize = 11.sp,
                    color = if (isAdmin) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAdmin) "ထွက်: -${dayExp.toInt()} Ks" else "ထွက်ငွေ: 🔒 Restricted",
                    fontSize = 11.sp,
                    color = if (isAdmin) BrandRose else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    transactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.group,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = tx.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (tx.type == "ဝင်ငွေ") "+" else "-"}${tx.amount.toInt()} Ks",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "ဝင်ငွေ") Color(0xFF2E7D32) else BrandRose
                                )

                                if (tx.repairId == null) {
                                    IconButton(
                                        onClick = { onEditTransaction(tx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteTransaction(tx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandRose)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceEditModal(
    transaction: TransactionEntity?,
    initialType: String,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: initialType) }
    var group by remember { mutableStateOf(transaction?.group ?: "Service") }
    var name by remember { mutableStateOf(transaction?.name ?: "") }
    var amountText by remember { mutableStateOf((transaction?.amount?.toInt() ?: 0).toString()) }
    var dateText by remember {
        mutableStateOf(
            transaction?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    val groupOptions = listOf("Service", "Sales", "Office", "Home", "Personal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (transaction == null) "$type စာရင်းသွင်းရန်" else "ငွေစာရင်း ပြင်ဆင်ရန်",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { type = "ဝင်ငွေ" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "ဝင်ငွေ") Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ဝင်ငွေ (+)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { type = "ထွက်ငွေ" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "ထွက်ငွေ") BrandRose else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ထွက်ငွေ (-)", fontSize = 11.sp)
                    }
                }

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        groupOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    group = opt
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("အကြောင်းအရာ *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("ပမာဏ (Ks) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("ရက်စွဲ (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val savedTx = TransactionEntity(
                        id = transaction?.id ?: 0L,
                        type = type,
                        group = group,
                        name = name.ifBlank { "Income/Expense" },
                        amount = amountText.toDoubleOrNull() ?: 0.0,
                        date = dateText
                    )
                    onSave(savedTx)
                }
            ) {
                Text("သိမ်းမည်", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("မလုပ်တော့ပါ")
            }
        }
    )
}
