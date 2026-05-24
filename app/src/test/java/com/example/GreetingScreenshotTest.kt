package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.ui.CategoryDonutChart
import com.example.ui.ExpenseScreen
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val categories = listOf("Food & Dining", "Transport & Auto", "Shopping")
    val spending = mapOf("Food & Dining" to 120.0, "Transport & Auto" to 80.0, "Shopping" to 50.0)

    composeTestRule.setContent {
      MyApplicationTheme {
        CategoryDonutChart(spending = spending, categories = categories)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun testExpenseScreenRendersWithoutCrash() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repository = ExpenseRepository(db.expenseDao(), db.budgetDao())
    val viewModel = ExpenseViewModel(repository)
    composeTestRule.setContent {
      MyApplicationTheme {
        ExpenseScreen(viewModel = viewModel)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/expense_screen.png")
  }
}
