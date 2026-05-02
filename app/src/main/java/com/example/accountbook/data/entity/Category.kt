package com.example.accountbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "",
    val type: String, // "EXPENSE" or "INCOME"
    val isPredefined: Boolean = true,
    val sortOrder: Int = 0
)
