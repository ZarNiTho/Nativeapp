package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MobileAnswerTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: AppViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            val voucherRepair by viewModel.voucherPreviewRepair.collectAsState()

            var showAiSheet by remember { mutableStateOf(false) }

            // Observe Toast Messages
            LaunchedEffect(Unit) {
                viewModel.toastMessage.collectLatest { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            MobileAnswerTheme(themeMode = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar(
                            selectedTheme = themeMode,
                            onThemeSelected = { viewModel.setThemeMode(it) },
                            userRole = currentUser.role,
                            onToggleRole = {
                                val nextRole = if (currentUser.role == "admin") "staff" else "admin"
                                viewModel.switchUserRole(nextRole)
                            },
                            onOpenAi = { showAiSheet = true }
                        )
                    },
                    bottomBar = {
                        AppBottomNavigation(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.setNavigationTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.INVENTORY -> InventoryScreen(viewModel)
                            NavigationTab.REPAIR -> RepairScreen(viewModel)
                            NavigationTab.AI_ASSISTANT -> AiTechnicalAssistantScreen(viewModel)
                            NavigationTab.FINANCE -> FinanceScreen(viewModel)
                            NavigationTab.ADMIN_LOGS -> AdminDashboardScreen(viewModel)
                        }

                        // AI Sheet Modal
                        if (showAiSheet) {
                            AiAssistantBottomSheet(
                                viewModel = viewModel,
                                onDismiss = { showAiSheet = false }
                            )
                        }

                        // Voucher Receipt Modal
                        voucherRepair?.let { repair ->
                            VoucherDialog(
                                repair = repair,
                                onDismiss = { viewModel.setVoucherPreview(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
