package com.example.accountbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.accountbook.data.dao.CategoryDao
import com.example.accountbook.data.dao.TransactionDao
import com.example.accountbook.data.entity.Category
import com.example.accountbook.data.entity.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "accountbook_database"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                seedCategories(database.categoryDao())
                            }
                        }
                    }
                })
                .build()
        }

        private suspend fun seedCategories(categoryDao: CategoryDao) {
            val expenseCategories = listOf(
                Category(name = "餐饮", icon = "restaurant", type = "EXPENSE"),
                Category(name = "交通", icon = "directions_bus", type = "EXPENSE"),
                Category(name = "购物", icon = "shopping_cart", type = "EXPENSE"),
                Category(name = "娱乐", icon = "movie", type = "EXPENSE"),
                Category(name = "住房", icon = "home", type = "EXPENSE"),
                Category(name = "通讯", icon = "phone", type = "EXPENSE"),
                Category(name = "医疗", icon = "local_hospital", type = "EXPENSE"),
                Category(name = "教育", icon = "school", type = "EXPENSE"),
                Category(name = "其他支出", icon = "more_horiz", type = "EXPENSE"),
            )
            val incomeCategories = listOf(
                Category(name = "工资", icon = "work", type = "INCOME"),
                Category(name = "奖金", icon = "star", type = "INCOME"),
                Category(name = "投资", icon = "trending_up", type = "INCOME"),
                Category(name = "兼职", icon = "handyman", type = "INCOME"),
                Category(name = "红包", icon = "redeem", type = "INCOME"),
                Category(name = "其他收入", icon = "more_horiz", type = "INCOME"),
            )
            categoryDao.insertAll(expenseCategories + incomeCategories)
        }
    }
}
