package com.example.accountbook.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.data.entity.Transaction
import com.example.accountbook.data.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllWithCategory(): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT * FROM transactions
        WHERE type = :type
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByTypeWithCategory(type: String): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT * FROM transactions
        WHERE date BETWEEN :startMillis AND :endMillis
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByDateRangeWithCategory(startMillis: Long, endMillis: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND date BETWEEN :startMillis AND :endMillis
    """)
    fun getTotalByTypeAndDate(type: String, startMillis: Long, endMillis: Long): Flow<Double>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Query("""
        SELECT * FROM transactions
        WHERE type = :type AND date BETWEEN :startMillis AND :endMillis
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByTypeAndDateRangeWithCategory(
        type: String, startMillis: Long, endMillis: Long
    ): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT * FROM transactions
        WHERE date BETWEEN :startMillis AND :endMillis
        ORDER BY date DESC, createdAt DESC
    """)
    suspend fun getByDateRangeSync(
        startMillis: Long, endMillis: Long
    ): List<TransactionWithCategory>

    @Delete
    suspend fun delete(transaction: Transaction)
}
