package com.example.accountbook.data.repository

import androidx.annotation.WorkerThread
import com.example.accountbook.data.dao.CategoryDao
import com.example.accountbook.data.dao.TransactionDao
import com.example.accountbook.data.entity.Category
import com.example.accountbook.data.entity.Transaction
import com.example.accountbook.data.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAll()

    fun getCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getByType(type)

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getById(id)

    suspend fun addCategory(category: Category): Long =
        categoryDao.insert(category)

    suspend fun updateCategory(category: Category) =
        categoryDao.update(category)

    suspend fun deleteCategory(category: Category) =
        categoryDao.delete(category)

    val allTransactions: Flow<List<TransactionWithCategory>> =
        transactionDao.getAllWithCategory()

    fun getTransactionsByType(type: String): Flow<List<TransactionWithCategory>> =
        transactionDao.getByTypeWithCategory(type)

    fun getTransactionsByDateRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<TransactionWithCategory>> =
        transactionDao.getByDateRangeWithCategory(startMillis, endMillis)

    fun getTotalByTypeAndDate(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<Double> =
        transactionDao.getTotalByTypeAndDate(type, startMillis, endMillis)

    @WorkerThread
    suspend fun addTransaction(transaction: Transaction): Long =
        transactionDao.insert(transaction)

    @WorkerThread
    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction)

    @WorkerThread
    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.delete(transaction)
}
