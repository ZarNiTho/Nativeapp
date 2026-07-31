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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.data.local.ProductEntity
import com.example.ui.AppViewModel
import com.example.ui.components.CustomSearchBar
import com.example.ui.components.FilterPill
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandLime
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandRose

@Composable
fun InventoryScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQueryInv.collectAsState()
    val filterCategory by viewModel.invCategoryFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showProductModal by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf("အားလုံး", "အထွေထွေ", "Hardwareအပိုပစ္စည်", "accessories", "ရောင်းပြီး")

    // Filter Logic
    val filteredProducts = remember(products, searchQuery, filterCategory) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (filterCategory) {
                "အားလုံး" -> p.status != "ရောင်းပြီး"
                "ရောင်းပြီး" -> p.status == "ရောင်းပြီး"
                else -> p.category == filterCategory && p.status != "ရောင်းပြီး"
            }
            matchesQuery && matchesCategory
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQueryInv.value = it },
                placeholder = "🔍 ပစ္စည်းအမည် ရှာရန်..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterPill(
                        label = cat,
                        isSelected = filterCategory == cat,
                        onClick = { viewModel.invCategoryFilter.value = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Product Button
            Button(
                onClick = {
                    editingProduct = null
                    showProductModal = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ ပစ္စည်းသစ် ထည့်ရန်", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Products List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ပစ္စည်းစာရင်း မရှိသေးပါ",
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
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            isAdmin = currentUser.role == "admin",
                            onSell = { viewModel.sellProduct(product, 1) },
                            onEdit = {
                                editingProduct = product
                                showProductModal = true
                            },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }

        // Product Modal Dialog
        if (showProductModal) {
            ProductEditModal(
                product = editingProduct,
                onDismiss = { showProductModal = false },
                onSave = { savedProduct ->
                    viewModel.saveProduct(savedProduct)
                    showProductModal = false
                }
            )
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    isAdmin: Boolean,
    onSell: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isSold = product.status == "ရောင်းပြီး"
    val isOutOfStock = product.status == "လက်ကျန်ပြတ်" || product.amount <= 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    text = product.status,
                    type = if (isSold) "danger" else if (isOutOfStock) "warning" else "success"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 ${product.amount} ခု | 📁 ${product.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isSold && !isOutOfStock) {
                    FilledTonalButton(
                        onClick = onSell,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("၁ ခုရောင်းမည်", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "အရင်း: ${product.costPrice.toInt()} Ks",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ရောင်း: ${product.price.toInt()} Ks",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
            }

            if (isAdmin && !isSold) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ပြင်မည်", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRose)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ဖျက်မည်", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditModal(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "အထွေထွေ") }
    var amountText by remember { mutableStateOf((product?.amount ?: 1).toString()) }
    var costPriceText by remember { mutableStateOf((product?.costPrice?.toInt() ?: 0).toString()) }
    var priceText by remember { mutableStateOf((product?.price?.toInt() ?: 0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "ပစ္စည်းစာရင်းသွင်းရန်" else "ပစ္စည်းစာရင်း ပြင်ရန်",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ပစ္စည်းအမည် *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("အမျိုးအစား") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("အရေအတွက်") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("အရင်း Ks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("ရောင်းဈေး Ks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toIntOrNull() ?: 1
                    val newProduct = ProductEntity(
                        id = product?.id ?: 0L,
                        name = name.ifBlank { "Unassigned Item" },
                        category = category.ifBlank { "အထွေထွေ" },
                        amount = amt,
                        costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                        price = priceText.toDoubleOrNull() ?: 0.0,
                        status = if (amt > 0) "ပစ္စည်းရှိ" else "လက်ကျန်ပြတ်"
                    )
                    onSave(newProduct)
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
