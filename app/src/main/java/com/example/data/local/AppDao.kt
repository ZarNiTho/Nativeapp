package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)
}

@Dao
interface RepairDao {
    @Query("SELECT * FROM repairs ORDER BY timestamp DESC")
    fun getAllRepairs(): Flow<List<RepairEntity>>

    @Query("SELECT * FROM repairs WHERE id = :id")
    suspend fun getRepairById(id: Long): RepairEntity?

    @Query("SELECT * FROM repairs WHERE vrno = :vrno LIMIT 1")
    suspend fun getRepairByVrNo(vrno: Long): RepairEntity?

    @Query("SELECT MAX(vrno) FROM repairs")
    suspend fun getMaxVrNo(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: RepairEntity): Long

    @Update
    suspend fun updateRepair(repair: RepairEntity)

    @Query("DELETE FROM repairs WHERE id = :id")
    suspend fun deleteRepairById(id: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY exactDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE repairId = :repairId")
    suspend fun getTransactionsByRepairId(repairId: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE repairId = :repairId")
    suspend fun deleteByRepairId(repairId: Long)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY lastOnline DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)
}
