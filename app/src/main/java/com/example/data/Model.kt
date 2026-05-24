package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val dateInMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val isIncome: Boolean = false
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: String, // "Total" for global monthly limit, or category names like "Food"
    val limitAmount: Double
)
