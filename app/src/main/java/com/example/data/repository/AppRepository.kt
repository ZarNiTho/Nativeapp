package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.AiActionParsed
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val db: AppDatabase) {

    val allProducts: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val allRepairs: Flow<List<RepairEntity>> = db.repairDao().getAllRepairs()
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allLogs: Flow<List<ActivityLogEntity>> = db.activityLogDao().getAllLogs()
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()

    suspend fun logActivity(user: String, action: String, details: String) {
        db.activityLogDao().insertLog(
            ActivityLogEntity(user = user, action = action, details = details)
        )
    }

    // --- Products ---
    suspend fun saveProduct(product: ProductEntity, currentUser: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (product.id == 0L) {
            val insertedId = db.productDao().insertProduct(product.copy(createdAt = System.currentTimeMillis()))
            logActivity(currentUser, "Add Stock", "${product.name} (Qty: ${product.amount}) စာရင်းသွင်းသည်")
        } else {
            db.productDao().updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
            logActivity(currentUser, "Edit Stock", "${product.name} စာရင်းပြင်ဆင်သည်")
        }
    }

    suspend fun sellProduct(product: ProductEntity, qtyToSell: Int, currentUser: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val remainQty = (product.amount - qtyToSell).coerceAtLeast(0)
        val updatedProduct = product.copy(
            amount = remainQty,
            status = if (remainQty > 0) "ပစ္စည်းရှိ" else "လက်ကျန်ပြတ်",
            updatedAt = System.currentTimeMillis()
        )
        db.productDao().updateProduct(updatedProduct)

        // Record Sold product history entry
        val soldEntity = ProductEntity(
            name = "${product.name} (ရောင်းပြီး)",
            category = product.category,
            amount = qtyToSell,
            costPrice = product.costPrice * qtyToSell,
            price = product.price * qtyToSell,
            status = "ရောင်းပြီး",
            sellDate = todayStr,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.productDao().insertProduct(soldEntity)

        // Add transaction entry
        val totalRevenue = product.price * qtyToSell
        db.transactionDao().insertTransaction(
            TransactionEntity(
                type = "ဝင်ငွေ",
                group = "Sales",
                name = "ရောင်းရငွေ: ${product.name} ($qtyToSell ခု)",
                amount = totalRevenue,
                date = todayStr,
                exactDate = System.currentTimeMillis()
            )
        )

        logActivity(currentUser, "Sell Stock", "${product.name} ($qtyToSell ခု) ရောင်းချခဲ့သည် (Ks $totalRevenue)")
    }

    suspend fun deleteProduct(product: ProductEntity, currentUser: String) {
        db.productDao().deleteProduct(product)
        logActivity(currentUser, "Delete Stock", "${product.name} ကို ဖျက်ဆီးခဲ့သည်")
    }

    // --- Repairs ---
    suspend fun getNextVrNo(): Long {
        val maxVr = db.repairDao().getMaxVrNo() ?: 0L
        return if (maxVr < 100) 101L else maxVr + 1
    }

    suspend fun saveRepair(repair: RepairEntity, currentUser: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isNew = repair.id == 0L

        if (isNew) {
            val vrNo = if (repair.vrno <= 0) getNextVrNo() else repair.vrno
            val newRepair = repair.copy(vrno = vrNo, timestamp = System.currentTimeMillis())
            val repairId = db.repairDao().insertRepair(newRepair)
            logActivity(currentUser, "New Repair", "VR:${vrNo} (${repair.name} - ${repair.model}) လက်ခံရရှိသည်")
        } else {
            db.repairDao().updateRepair(repair.copy(timestamp = System.currentTimeMillis()))
            logActivity(currentUser, "Update Repair", "VR:${repair.vrno} Status/Details ပြင်ဆင်သည်: ${repair.status}")

            // Synchronize with Transactions
            val existingTxs = db.transactionDao().getTransactionsByRepairId(repair.id)
            val incTx = existingTxs.firstOrNull { it.type == "ဝင်ငွေ" }

            if (repair.status.contains("ထုတ်ယူပြီး") && !repair.status.contains("ပြင်မရ")) {
                if (repair.income > 0) {
                    if (incTx != null) {
                        db.transactionDao().updateTransaction(
                            incTx.copy(
                                amount = repair.income,
                                name = "VR:${repair.vrno} ပြင်ခ (${repair.name})"
                            )
                        )
                    } else {
                        db.transactionDao().insertTransaction(
                            TransactionEntity(
                                type = "ဝင်ငွေ",
                                group = "Service",
                                name = "VR:${repair.vrno} ပြင်ခ (${repair.name})",
                                amount = repair.income,
                                date = repair.date,
                                repairId = repair.id,
                                exactDate = System.currentTimeMillis()
                            )
                        )
                    }
                } else if (incTx != null) {
                    db.transactionDao().deleteTransaction(incTx)
                }
            } else {
                if (incTx != null) {
                    db.transactionDao().deleteTransaction(incTx)
                }
            }
        }
    }

    suspend fun voidRepair(repairId: Long, currentUser: String) {
        val repair = db.repairDao().getRepairById(repairId) ?: return
        db.transactionDao().deleteByRepairId(repairId)
        val voidedRepair = repair.copy(
            status = "ပယ်ဖျက် (Void)",
            timestamp = System.currentTimeMillis()
        )
        db.repairDao().updateRepair(voidedRepair)
        logActivity(currentUser, "Void Repair", "VR:${repair.vrno} စာရင်းကို ပယ်ဖျက် (Void) ခဲ့သည်")
    }

    // --- Transactions ---
    suspend fun saveTransaction(transaction: TransactionEntity, currentUser: String) {
        if (transaction.id == 0L) {
            db.transactionDao().insertTransaction(transaction)
            logActivity(currentUser, "Add Finance", "${transaction.type} ${transaction.name} (Ks ${transaction.amount})")
        } else {
            db.transactionDao().updateTransaction(transaction)
            logActivity(currentUser, "Edit Finance", "${transaction.type} ${transaction.name} ပြင်ဆင်သည်")
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity, currentUser: String) {
        db.transactionDao().deleteTransaction(transaction)
        logActivity(currentUser, "Delete Finance", "${transaction.name} ဖျက်ဆီးခဲ့သည်")
    }

    // --- Users ---
    suspend fun updateUserRole(user: UserEntity, newRole: String, currentUser: String) {
        db.userDao().updateUser(user.copy(role = newRole, lastOnline = System.currentTimeMillis()))
        logActivity(currentUser, "User Role Change", "${user.username} role changed to $newRole")
    }

    // --- Gemini AI Interpreter Execute ---
    suspend fun askTechnicalRepair(userText: String, repairContext: String? = null): String {
        return GeminiClient.askTechnicalRepairAssistant(userText, repairContext)
    }

    suspend fun interpretAndExecuteAiCommand(userText: String, currentUser: String): String {
        // If query looks like a technical question or troubleshooting ask
        val techKeywords = listOf("how to", "diagnostic", "short circuit", "voltage", "ic", "charging", "no power", "bootloop", "repair", "multimeter", "diode", "screen", "display", "battery", "လဲ", "ပြင်", "ရှော့", "ပါဝါ", "ဘက်ထရီ", "အားမဝင်")
        val isTechnical = techKeywords.any { userText.lowercase().contains(it) }

        if (isTechnical) {
            return GeminiClient.askTechnicalRepairAssistant(userText)
        }

        val parsed = GeminiClient.interpretCommand(userText) ?: return "⚠️ AI နားမလည်ပါ သို့မဟုတ် Error ဖြစ်ပွားပါသည်"

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return when (parsed.action) {
            "chat" -> {
                if (!parsed.message.isNullOrBlank() && !parsed.message.contains("ကျေးဇူးပြု၍")) {
                    parsed.message
                } else {
                    GeminiClient.askTechnicalRepairAssistant(userText)
                }
            }
            "new_repair" -> {
                val nextVr = getNextVrNo()
                val repair = RepairEntity(
                    vrno = nextVr,
                    date = todayStr,
                    name = parsed.name ?: "Unknown",
                    phone = parsed.phone ?: "",
                    model = parsed.model ?: "Smartphone",
                    issue = parsed.issue ?: "စစ်ဆေးပြုပြင်ရန်",
                    status = "စစ်ဆေးပြုပြင်နေဆဲ"
                )
                saveRepair(repair, currentUser)
                "✅ ${repair.name} ၏ ${repair.model} ဖုန်းအသစ်အပ်နှံခြင်း အောင်မြင်ပါသည်။ (VR No: $nextVr)"
            }
            "finance" -> {
                val tx = TransactionEntity(
                    type = parsed.type ?: "ဝင်ငွေ",
                    group = parsed.group ?: "Service",
                    name = parsed.name ?: "AI Entry",
                    amount = parsed.amount ?: 0.0,
                    date = todayStr
                )
                saveTransaction(tx, currentUser)
                "✅ ${tx.type} စာရင်းသွင်းပြီးပါပြီ။ (${tx.amount} Ks)"
            }
            else -> parsed.message ?: "လုပ်ဆောင်ချက် အောင်မြင်ပါသည်"
        }
    }
}
