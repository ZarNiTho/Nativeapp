package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.AppRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class NavigationTab {
    INVENTORY, REPAIR, AI_ASSISTANT, FINANCE, ADMIN_LOGS
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val repairContext: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(AppDatabase.getDatabase(application))

    // Theme & Navigation
    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.INVENTORY)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Current User State
    private val _currentUser = MutableStateFlow(
        UserEntity(id = 1, username = "Admin", email = "admin@mobileanswer.com", role = "admin")
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    // Filters
    val searchQueryInv = MutableStateFlow("")
    val invCategoryFilter = MutableStateFlow("အားလုံး")

    val searchQueryRep = MutableStateFlow("")
    val repStatusFilter = MutableStateFlow("အားလုံး")

    val selectedFinanceMonth = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    )

    val financeDateStart = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-01", Locale.getDefault()).format(Date())
    )
    val financeDateEnd = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )

    // Data Flows from Room
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repairs: StateFlow<List<RepairEntity>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Chat State
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                text = "မင်္ဂလာပါ 👋 Mobile ANSWER AI Technical Assistant မှ ကြိုဆိုပါသည်!\nဖုန်းပြုပြင်ခြင်းဆိုင်ရာ နည်းပညာ မေးခွန်းများ (No Power, Short Circuit, Charging IC, Software Flashing) နှင့် ဆိုင်လုပ်ငန်းဆောင်တာများကို မေးမြန်းနိုင်ပါသည်။",
                isUser = false
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    // Voucher Preview Modal State
    private val _voucherPreviewRepair = MutableStateFlow<RepairEntity?>(null)
    val voucherPreviewRepair: StateFlow<RepairEntity?> = _voucherPreviewRepair.asStateFlow()

    // Status Toast / Message
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Actions
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setNavigationTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun switchUserRole(newRole: String) {
        _currentUser.value = _currentUser.value.copy(role = newRole)
        emitToast("User role changed to $newRole")
    }

    fun setVoucherPreview(repair: RepairEntity?) {
        _voucherPreviewRepair.value = repair
    }

    // --- Inventory Actions ---
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product, _currentUser.value.username)
            emitToast("ပစ္စည်းစာရင်း သိမ်းဆည်းပြီးပါပြီ")
        }
    }

    fun sellProduct(product: ProductEntity, qtyToSell: Int = 1) {
        viewModelScope.launch {
            repository.sellProduct(product, qtyToSell, _currentUser.value.username)
            emitToast("${product.name} $qtyToSell ခု ရောင်းချပြီးပါပြီ")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product, _currentUser.value.username)
            emitToast("ပစ္စည်းစာရင်း ဖျက်ဆီးပြီးပါပြီ")
        }
    }

    // --- Repair Actions ---
    fun saveRepair(repair: RepairEntity) {
        viewModelScope.launch {
            repository.saveRepair(repair, _currentUser.value.username)
            emitToast("ဖုန်းပြင်မှတ်တမ်း သိမ်းပြီးပါပြီ")
        }
    }

    fun voidRepair(repairId: Long) {
        viewModelScope.launch {
            repository.voidRepair(repairId, _currentUser.value.username)
            emitToast("ဖုန်းပြင်စာရင်း ပယ်ဖျက် (Void) ပြီးပါပြီ")
        }
    }

    // --- Finance Actions ---
    fun saveTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.saveTransaction(transaction, _currentUser.value.username)
            emitToast("${transaction.type} စာရင်း သိမ်းပြီးပါပြီ")
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction, _currentUser.value.username)
            emitToast("ငွေစာရင်း ဖျက်ပြီးပါပြီ")
        }
    }

    // --- Admin User Roles ---
    fun updateUserRole(user: UserEntity, newRole: String) {
        viewModelScope.launch {
            repository.updateUserRole(user, newRole, _currentUser.value.username)
            emitToast("${user.username} role $newRole သို့ ပြောင်းပြီးပါပြီ")
        }
    }

    fun clearAiMessages() {
        _aiMessages.value = listOf(
            AiChatMessage(
                text = "မင်္ဂလာပါ 👋 Mobile ANSWER AI Technical Assistant မှ ကြိုဆိုပါသည်!\nဖုန်းပြုပြင်ခြင်းဆိုင်ရာ နည်းပညာ မေးခွန်းများ (No Power, Short Circuit, Charging IC, Software Flashing) နှင့် ဆိုင်လုပ်ငန်းဆောင်တာများကို မေးမြန်းနိုင်ပါသည်။",
                isUser = false
            )
        )
    }

    // --- Gemini AI Speech & Chat ---
    fun sendAiUserMessage(inputText: String, repairContext: String? = null) {
        if (inputText.isBlank()) return
        val userMsg = AiChatMessage(text = inputText, isUser = true, repairContext = repairContext)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiProcessing.value = true

        viewModelScope.launch {
            val responseText = if (!repairContext.isNullOrBlank()) {
                repository.askTechnicalRepair(inputText, repairContext)
            } else {
                repository.interpretAndExecuteAiCommand(
                    inputText,
                    _currentUser.value.username
                )
            }
            val systemMsg = AiChatMessage(text = responseText, isUser = false)
            _aiMessages.value = _aiMessages.value + systemMsg
            _isAiProcessing.value = false
        }
    }

    private fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }
}
