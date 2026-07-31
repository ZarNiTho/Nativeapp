package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        ProductEntity::class,
        RepairEntity::class,
        TransactionEntity::class,
        ActivityLogEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun repairDao(): RepairDao
    abstract fun transactionDao(): TransactionDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobile_answer_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            // Seed Users
            db.userDao().insertUser(UserEntity(username = "Admin", email = "admin@mobileanswer.com", role = "admin"))
            db.userDao().insertUser(UserEntity(username = "Staff User", email = "staff@mobileanswer.com", role = "staff"))

            // Seed Products
            db.productDao().insertProduct(
                ProductEntity(
                    name = "iPhone 11 Battery Original",
                    category = "Hardwareအပိုပစ္စည်",
                    amount = 5,
                    costPrice = 28000.0,
                    price = 38000.0,
                    status = "ပစ္စည်းရှိ"
                )
            )
            db.productDao().insertProduct(
                ProductEntity(
                    name = "Samsung A52 Display LCD Screen",
                    category = "Hardwareအပိုပစ္စည်",
                    amount = 3,
                    costPrice = 45000.0,
                    price = 65000.0,
                    status = "ပစ္စည်းရှိ"
                )
            )
            db.productDao().insertProduct(
                ProductEntity(
                    name = "Type-C Fast Charging Cable 65W",
                    category = "accessories",
                    amount = 12,
                    costPrice = 3500.0,
                    price = 7500.0,
                    status = "ပစ္စည်းရှိ"
                )
            )
            db.productDao().insertProduct(
                ProductEntity(
                    name = "Remax 20000mAh Powerbank",
                    category = "accessories",
                    amount = 4,
                    costPrice = 22000.0,
                    price = 32000.0,
                    status = "ပစ္စည်းရှိ"
                )
            )
            db.productDao().insertProduct(
                ProductEntity(
                    name = "iPhone 13 Pro Back Glass Cover",
                    category = "အထွေထွေ",
                    amount = 2,
                    costPrice = 12000.0,
                    price = 22000.0,
                    status = "ပစ္စည်းရှိ"
                )
            )

            // Seed Repairs
            db.repairDao().insertRepair(
                RepairEntity(
                    vrno = 101,
                    date = todayStr,
                    name = "ဦးဝင်းနိုင်",
                    phone = "09450012345",
                    location = "ရန်ကုန်",
                    model = "iPhone 11",
                    issue = "ဘက်ထရီ လဲရန်",
                    remark = "Original ဘက်ထရီ တောင်းထားသည်",
                    status = "ပြင်ပြီး-မရွေးသေး",
                    cost = 28000.0,
                    income = 48000.0
                )
            )
            db.repairDao().insertRepair(
                RepairEntity(
                    vrno = 102,
                    date = todayStr,
                    name = "ဒေါ်နုနု",
                    phone = "09250098765",
                    location = "မန္တလေး",
                    model = "Samsung A52",
                    issue = "မှန်ကွဲ မှန်လဲရန်",
                    remark = "Original LCD",
                    status = "ထုတ်ယူပြီး-အောင်မြင်",
                    cost = 45000.0,
                    income = 75000.0
                )
            )
            db.repairDao().insertRepair(
                RepairEntity(
                    vrno = 103,
                    date = todayStr,
                    name = "ကိုအောင်ဇော်",
                    phone = "09780112233",
                    location = "ရန်ကုန်",
                    model = "Xiaomi Redmi Note 10",
                    issue = "Power မပွင့်ပါ / ရေဝင်ထားသည်",
                    remark = "စစ်ဆေးဆဲ",
                    status = "စစ်ဆေးပြုပြင်နေဆဲ",
                    cost = 0.0,
                    income = 0.0
                )
            )

            // Seed Transactions
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    type = "ဝင်ငွေ",
                    group = "Service",
                    name = "VR:102 ပြင်ခ (ဒေါ်နုနု)",
                    amount = 75000.0,
                    date = todayStr,
                    repairId = 2
                )
            )
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    type = "ဝင်ငွေ",
                    group = "Sales",
                    name = "ရောင်းရငွေ: Type-C Fast Cable x2",
                    amount = 15000.0,
                    date = todayStr
                )
            )
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    type = "ထွက်ငွေ",
                    group = "Office",
                    name = "ဆိုင် မီတာခ + အင်တာနက်ခ",
                    amount = 25000.0,
                    date = todayStr
                )
            )

            // Seed Activity Logs
            db.activityLogDao().insertLog(
                ActivityLogEntity(
                    user = "Admin",
                    action = "System Initialized",
                    details = "Mobile ANSWER Native Android App System Started"
                )
            )
        }
    }
}
