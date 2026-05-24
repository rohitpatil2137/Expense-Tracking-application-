package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun getBudgetByCategory(category: String): Budget? {
        return budgetDao.getBudgetByCategory(category)
    }
}
