package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "အထွေထွေ", "Hardwareအပိုပစ္စည်", "accessories"
    val amount: Int,
    val costPrice: Double,
    val price: Double,
    val status: String, // "ပစ္စည်းရှိ", "လက်ကျန်ပြတ်", "ရောင်းပြီး"
    val sellDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val historyJson: String = "[]" // JSON string of activity logs
)

@Entity(tableName = "repairs")
data class RepairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vrno: Long,
    val date: String, // YYYY-MM-DD
    val name: String,
    val phone: String = "",
    val location: String = "",
    val model: String,
    val issue: String,
    val remark: String = "",
    val status: String = "စစ်ဆေးပြုပြင်နေဆဲ", 
    // "စစ်ဆေးပြုပြင်နေဆဲ", "ပြင်ပြီး-မရွေးသေး", "ပြင်မရ-မရွေးသေး", "ထုတ်ယူပြီး-အောင်မြင်", "ထုတ်ယူပြီး-ပြင်မရ", "ပယ်ဖျက် (Void)"
    val cost: Double = 0.0,
    val income: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val historyJson: String = "[]"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "ဝင်ငွေ", "ထွက်ငွေ"
    val group: String, // "Service", "Sales", "Office", "Home", "Personal"
    val name: String,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val repairId: Long? = null,
    val exactDate: Long = System.currentTimeMillis(),
    val historyJson: String = "[]"
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val role: String, // "admin", "staff"
    val lastOnline: Long = System.currentTimeMillis()
)
