package com.example.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Budget
import com.example.data.Expense
import com.example.data.ExpenseRepository
import com.example.data.ParsedTransaction
import com.example.data.SmsTransactionParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalBudget: Double = 1000.0, // Default if not found in DB
    val categorySpending: Map<String, Double> = emptyMap(),
    val categoryIncome: Map<String, Double> = emptyMap(),
    val categoryBudgets: Map<String, Double> = emptyMap(),
    val selectedYear: Int = 2026,
    val selectedMonth: Int = Calendar.MAY, // Match current local year/month, but interactive
    val filterCategory: String? = null,
    val searchQuery: String = ""
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _filterCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val categories = listOf(
        "Food & Dining",
        "Transport & Auto",
        "Shopping",
        "Entertainment",
        "Utilities & Bills",
        "Health & Wellness",
        "Education",
        "Miscellaneous"
    )

    val incomeCategories = listOf(
        "Salary",
        "Credit Card",
        "Slice",
        "Other Income"
    )

    // Main uiState combining repository data and selection configurations
    val uiState: StateFlow<ExpenseUiState> = combine(
        combine(repository.allExpenses, repository.allBudgets) { exp, bud -> Pair(exp, bud) },
        combine(_selectedYear, _selectedMonth) { yr, mth -> Pair(yr, mth) },
        combine(_filterCategory, _searchQuery) { cat, q -> Pair(cat, q) }
    ) { d1, d2, d3 ->
        val allExpenses = d1.first
        val budgets = d1.second
        val year = d2.first
        val month = d2.second
        val filterCat = d3.first
        val search = d3.second
        
        // Calculate timestamp range of the selected month
        val bounds = getMonthBounds(year, month)
        val startMillis = bounds.first
        val endMillis = bounds.second

        // Filter work
        val thisMonthExpenses = allExpenses.filter { expense ->
            expense.dateInMillis in startMillis..endMillis
        }

        val filteredExpenses = thisMonthExpenses.filter { expense ->
            val matchesCategory = filterCat == null || expense.category == filterCat
            val matchesSearch = search.isEmpty() || 
                    expense.title.contains(search, ignoreCase = true) ||
                    (expense.note.contains(search, ignoreCase = true))
            matchesCategory && matchesSearch
        }

        // Aggregate spend and income
        val totalSpent = thisMonthExpenses.filter { !it.isIncome }.sumOf { it.amount }
        val totalIncome = thisMonthExpenses.filter { it.isIncome }.sumOf { it.amount }

        // Category spending map (expenses only)
        val categorySpending = categories.associateWith { category ->
            thisMonthExpenses.filter { !it.isIncome && it.category == category }.sumOf { it.amount }
        }

        // Category income map (income only)
        val categoryIncome = incomeCategories.associateWith { category ->
            thisMonthExpenses.filter { it.isIncome && it.category == category }.sumOf { it.amount }
        }

        // Budget extraction
        val totalBudgetAmount = budgets.find { it.category == "Total" }?.limitAmount ?: 1000.0
        val categoryBudgets = budgets.filter { it.category != "Total" }.associate { it.category to it.limitAmount }

        ExpenseUiState(
            expenses = filteredExpenses,
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            totalBudget = totalBudgetAmount,
            categorySpending = categorySpending,
            categoryIncome = categoryIncome,
            categoryBudgets = categoryBudgets,
            selectedYear = year,
            selectedMonth = month,
            filterCategory = filterCat,
            searchQuery = search
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    // User Operations
    fun addExpense(title: String, amount: Double, category: String, dateInMillis: Long, note: String, isIncome: Boolean = false) {
        viewModelScope.launch {
            try {
                repository.insertExpense(
                    Expense(
                        title = title,
                        amount = amount,
                        category = category,
                        dateInMillis = dateInMillis,
                        note = note,
                        isIncome = isIncome
                    )
                )
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Error adding expense to repository", e)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Error deleting expense", e)
            }
        }
    }

    fun setTotalBudget(limit: Double) {
        viewModelScope.launch {
            try {
                repository.insertBudget(Budget("Total", limit))
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Error setting total budget", e)
            }
        }
    }

    fun setCategoryBudget(category: String, limit: Double) {
        viewModelScope.launch {
            try {
                repository.insertBudget(Budget(category, limit))
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Error setting category budget for $category", e)
            }
        }
    }

    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun scanDeviceSms(context: Context): List<ParsedTransaction> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ParsedTransaction>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("body", "date")
        
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC")
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndexOrThrow("body")
                val dateIndex = c.getColumnIndexOrThrow("date")
                
                var count = 0
                while (c.moveToNext() && count < 300) {
                    val body = c.getString(bodyIndex) ?: continue
                    val date = c.getLong(dateIndex)
                    val parsed = SmsTransactionParser.parseMessage(body, date)
                    if (parsed != null) {
                        list.add(parsed)
                        if (list.size >= 15) break
                    }
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("ExpenseViewModel", "Error scanning SMS", e)
        }
        list
    }

    // Date computation helper
    private fun getMonthBounds(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        // Set to next month first, then subtract 1 milli
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        return Pair(start, end)
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
