package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    selectedTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    userRole: String,
    onToggleRole: () -> Unit,
    onOpenAi: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Mobile ANSWER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "PHONE SERVICE & STORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Theme Switcher Dropdown
                var themeMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    AssistChip(
                        onClick = { themeMenuExpanded = true },
                        label = {
                            Text(
                                text = when (selectedTheme) {
                                    AppThemeMode.LIGHT -> "☀️ Light"
                                    AppThemeMode.DARK -> "🌙 Dark"
                                    AppThemeMode.GOLD_CHARCOAL -> "✨ Gold"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = themeMenuExpanded,
                        onDismissRequest = { themeMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("☀️ Light Theme") },
                            onClick = {
                                onThemeSelected(AppThemeMode.LIGHT)
                                themeMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🌙 Dark Theme") },
                            onClick = {
                                onThemeSelected(AppThemeMode.DARK)
                                themeMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("✨ Gold Charcoal") },
                            onClick = {
                                onThemeSelected(AppThemeMode.GOLD_CHARCOAL)
                                themeMenuExpanded = false
                            }
                        )
                    }
                }

                // Role Toggle Chip
                AssistChip(
                    onClick = onToggleRole,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (userRole == "admin") BrandRose.copy(alpha = 0.15f) else BrandSky.copy(alpha = 0.15f),
                        labelColor = if (userRole == "admin") BrandRose else BrandSky
                    ),
                    label = {
                        Text(
                            text = if (userRole == "admin") "🛡️ Admin" else "👤 Staff",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )

                // AI Button
                IconButton(
                    onClick = onOpenAi,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary)
                ) {
                    Text(text = "🤖", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.INVENTORY,
            onClick = { onTabSelected(NavigationTab.INVENTORY) },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Stock") },
            label = { Text("Stock", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.REPAIR,
            onClick = { onTabSelected(NavigationTab.REPAIR) },
            icon = { Icon(Icons.Default.Build, contentDescription = "Repair") },
            label = { Text("Repair", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.AI_ASSISTANT,
            onClick = { onTabSelected(NavigationTab.AI_ASSISTANT) },
            icon = { Text("🤖", fontSize = 18.sp) },
            label = { Text("AI Guide", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.FINANCE,
            onClick = { onTabSelected(NavigationTab.FINANCE) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finance") },
            label = { Text("Finance", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.ADMIN_LOGS,
            onClick = { onTabSelected(NavigationTab.ADMIN_LOGS) },
            icon = { Icon(Icons.Default.Analytics, contentDescription = "Logs") },
            label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
    }
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    type: String = "info"
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val (bgColor, textColor) = when (type) {
        "success" -> BrandLime.copy(alpha = if (isDark) 0.2f else 0.15f) to if (isDark) BrandLime else Color(0xFF047857)
        "danger" -> BrandRose.copy(alpha = if (isDark) 0.2f else 0.15f) to if (isDark) BrandRose else Color(0xFFBE123C)
        "warning" -> BrandGold.copy(alpha = if (isDark) 0.2f else 0.15f) to if (isDark) BrandGold else Color(0xFFB45309)
        else -> BrandSky.copy(alpha = if (isDark) 0.2f else 0.15f) to if (isDark) BrandSky else Color(0xFF0369A1)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
