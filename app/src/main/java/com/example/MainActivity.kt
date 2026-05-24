package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.ui.ExpenseScreen
import com.example.ui.ExpenseViewModel
import com.example.ui.ExpenseViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Database and Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ExpenseRepository(database.expenseDao(), database.budgetDao())
    
    // Wire up ViewModel using Simple Constructor Factory
    val viewModel: ExpenseViewModel by viewModels {
      ExpenseViewModelFactory(repository)
    }

    setContent {
      MyApplicationTheme {
        ExpenseScreen(viewModel = viewModel)
      }
    }
  }
}
