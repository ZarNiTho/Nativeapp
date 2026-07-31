package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RepairEntity
import com.example.ui.AiChatMessage
import com.example.ui.AppViewModel
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandLime
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSky
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTechnicalAssistantScreen(viewModel: AppViewModel) {
    val messages by viewModel.aiMessages.collectAsState()
    val isProcessing by viewModel.isAiProcessing.collectAsState()
    val repairs by viewModel.repairs.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var selectedRepairContext by remember { mutableStateOf<RepairEntity?>(null) }
    var showContextDropdown by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Technical Assistant Top Header Card ---
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemini AI Technical Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                            Text(
                                text = "ဖုန်းပြင်ဆင်ခြင်းဆိုင်ရာ နည်းပညာဆရာလမ်းညွှန်",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { viewModel.clearAiMessages() }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Repair Device Context Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { showContextDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    tint = BrandGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedRepairContext != null)
                                        "🎯 ဂျော့စုံစမ်းရန်: VR:${selectedRepairContext?.vrno} - ${selectedRepairContext?.model} (${selectedRepairContext?.name})"
                                    else "📌 ပြင်ဆင်ဆဲဖုန်း ဂျော့ကို ရွေးချယ်ပြီး မေးမြန်းရန် (Optional)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedRepairContext != null) BrandGold else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (selectedRepairContext != null) {
                                IconButton(
                                    onClick = { selectedRepairContext = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Context", modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showContextDropdown,
                        onDismissRequest = { showContextDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 260.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("❌ သီးခြားဖုန်း ရွေးမထားပါ (General Question)", fontSize = 12.sp) },
                            onClick = {
                                selectedRepairContext = null
                                showContextDropdown = false
                            }
                        )
                        Divider()
                        repairs.filter { !it.status.contains("ထုတ်ယူပြီး") }.forEach { rep ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("VR:${rep.vrno} - ${rep.model} (${rep.name})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("ပြဿနာ: ${rep.issue} | အခြေအနေ: ${rep.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedRepairContext = rep
                                    showContextDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- Chat Messages Area ---
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg, onCopyText = { textToCopy ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Response", textToCopy))
                    Toast.makeText(context, "ကူးယူပြီးပါပြီ 📋", Toast.LENGTH_SHORT).show()
                })
            }

            if (isProcessing) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = BrandPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Gemini AI နည်းပညာ အချက်အလက်များ ရှာဖွေတွက်ချက်နေပါသည်...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- Quick Technical Repair Suggestion Chips ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            Text(
                text = "💡 Quick Diagnostic Suggestions (နှိပ်၍ မေးမြန်းပါ):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SuggestionChip(
                        onClick = {
                            val contextStr = selectedRepairContext?.let { "VR:${it.vrno} ${it.model} - ${it.issue}" }
                            viewModel.sendAiUserMessage("iPhone ပါဝါမနိုး/ Short Circuit ဘယ်လို တိုင်းတာ စစ်ဆေးရမလဲ", contextStr)
                        },
                        label = { Text("⚡ No Power / Short Circuit", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BrandGold.copy(alpha = 0.12f))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            val contextStr = selectedRepairContext?.let { "VR:${it.vrno} ${it.model} - ${it.issue}" }
                            viewModel.sendAiUserMessage("ဖုန်း အားသွင်းမဝင်၊ Charging IC & VBUS 5V စစ်ဆေးနည်း", contextStr)
                        },
                        label = { Text("🔌 Not Charging / VBUS", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BrandSky.copy(alpha = 0.12f))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            val contextStr = selectedRepairContext?.let { "VR:${it.vrno} ${it.model} - ${it.issue}" }
                            viewModel.sendAiUserMessage("Screen AMOLED Lines / Touch မလုပ်ရင် Multimeter Diode Mode စစ်နည်း", contextStr)
                        },
                        label = { Text("📱 Display & Touch IC", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BrandLime.copy(alpha = 0.12f))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            val contextStr = selectedRepairContext?.let { "VR:${it.vrno} ${it.model} - ${it.issue}" }
                            viewModel.sendAiUserMessage("Xiaomi / Samsung Bootloop ဖြစ်နေရင် Software Flash/Bypass လုပ်နည်း", contextStr)
                        },
                        label = { Text("💻 Bootloop / Software Flash", fontSize = 11.sp) }
                    )
                }
            }
        }

        // --- Bottom Conversational Input Bar ---
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (selectedRepairContext != null)
                                "VR:${selectedRepairContext?.vrno} အတွက် နည်းပညာ မေးမြန်းပါ..."
                            else "နည်းပညာ မေးခွန်း သို့ ဆိုင်အမိန့် ရိုက်ပါ...",
                            fontSize = 12.sp
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            val contextStr = selectedRepairContext?.let { "VR:${it.vrno} ${it.model} (${it.issue})" }
                            viewModel.sendAiUserMessage(inputText, contextStr)
                            inputText = ""
                        }
                    }),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Voice / Mic Simulate button
                IconButton(
                    onClick = {
                        inputText = "iPhone 11 ပါဝါမပွင့်ပါ ဘယ်လိုစစ်ရမလဲ"
                        Toast.makeText(context, "🎤 Voice command sample filled!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = BrandPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val contextStr = selectedRepairContext?.let { "VR:${it.vrno} ${it.model} (${it.issue})" }
                            viewModel.sendAiUserMessage(inputText, contextStr)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isProcessing,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) BrandPrimary else Color.Gray.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: AiChatMessage,
    onCopyText: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            if (!message.repairContext.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandGold.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "🎯 Context: ${message.repairContext}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (message.isUser) 18.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 18.dp
                ),
                color = if (message.isUser) BrandPrimary
                else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = if (message.isUser) 2.dp else 1.dp,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!message.isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🤖", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Gemini Technical AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }

                            IconButton(
                                onClick = { onCopyText(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
