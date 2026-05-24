package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog as M3DatePickerDialog
import com.example.data.ParsedTransaction
import com.example.data.SmsTransactionParser
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalPlay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Map categories to modern visual icons
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food & Dining" -> Icons.Default.Fastfood
        "Transport & Auto" -> Icons.Default.Commute
        "Shopping" -> Icons.Default.LocalMall
        "Entertainment" -> Icons.Default.LocalPlay
        "Utilities & Bills" -> Icons.Default.ElectricBolt
        "Health & Wellness" -> Icons.Default.Favorite
        "Education" -> Icons.Default.School
        "Salary" -> Icons.Default.MonetizationOn
        "Credit Card" -> Icons.Default.CreditCard
        "Slice" -> Icons.Default.Payments
        "Other Income" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Help
    }
}

// Map categories to standard chart colors
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food & Dining" -> Color(0xFF4CAF50) // Green
        "Transport & Auto" -> Color(0xFF2196F3) // Blue
        "Shopping" -> Color(0xFFFF9800) // Orange
        "Entertainment" -> Color(0xFF9C27B0) // Purple
        "Utilities & Bills" -> Color(0xFFE91E63) // Pink
        "Health & Wellness" -> Color(0xFF00BCD4) // Cyan
        "Education" -> Color(0xFF673AB7) // Deep Purple
        "Salary" -> Color(0xFF2E7D32) // Forest Green
        "Credit Card" -> Color(0xFF1565C0) // Indigo Blue
        "Slice" -> Color(0xFFAD1457) // Dark Pink / Rose
        "Other Income" -> Color(0xFF00695C) // Teal
        else -> Color(0xFF9E9E9E) // Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Expense Tracker",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Control your money",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    // Modern Month Picker Selector
                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                showMonthYearPicker = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Month",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${monthNames[uiState.selectedMonth]} ${uiState.selectedYear}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_expense_fab")
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            
            // ----------------------------------------------------
            // BUDGET VISUALIZER & ALERTS HEADER CHIP
            // ----------------------------------------------------
            BudgetOverviewHeader(
                totalSpent = uiState.totalSpent,
                totalIncome = uiState.totalIncome,
                totalBudget = uiState.totalBudget,
                onEditBudgetClick = { showBudgetDialog = true }
            )

            // Tabs to alternate views cleanly within a gorgeous Single-Screen Context
            val tabs = listOf("Expenses", "Charts", "Category Budgets")
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> ExpensesTab(
                        uiState = uiState,
                        viewModel = viewModel,
                        categories = viewModel.categories + viewModel.incomeCategories
                    )
                    1 -> ChartsTab(
                        uiState = uiState,
                        viewModel = viewModel,
                        categories = viewModel.categories
                    )
                    2 -> CategoryBudgetsTab(
                        uiState = uiState,
                        viewModel = viewModel,
                        categories = viewModel.categories
                    )
                }
            }
        }

        // Add Expense Dialog Model
        if (showAddDialog) {
            AddExpenseDialog(
                viewModel = viewModel,
                categories = viewModel.categories,
                onDismiss = { showAddDialog = false }
            )
        }

        // Edit Total Monthly Budget Dialog Model
        if (showBudgetDialog) {
            EditBudgetDialog(
                currentBudget = uiState.totalBudget,
                onDismiss = { showBudgetDialog = false },
                onSave = { newBudget ->
                    viewModel.setTotalBudget(newBudget)
                }
            )
        }

        // Month Year Picker Dialog Model
        if (showMonthYearPicker) {
            MonthYearPickerDialog(
                currentYear = uiState.selectedYear,
                currentMonth = uiState.selectedMonth,
                onDismiss = { showMonthYearPicker = false },
                onSelect = { year, month ->
                    viewModel.selectMonth(year, month)
                    showMonthYearPicker = false
                }
            )
        }
    }
}

@Composable
fun BudgetOverviewHeader(
    totalSpent: Double,
    totalIncome: Double,
    totalBudget: Double,
    onEditBudgetClick: () -> Unit
) {
    val spendPercent = if (totalBudget > 0) totalSpent / totalBudget else 0.0
    val alertPercent = spendPercent * 100
    val netBalance = totalIncome - totalSpent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spent this Month",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${String.format("%.2f", totalSpent)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " / $${String.format("%.0f", totalBudget)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEditBudgetClick,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .testTag("edit_budget_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Premium income, expense, balance row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income Column
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+$${String.format("%.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterVertically)
                )

                // Expenses Column
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-$${String.format("%.2f", totalSpent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterVertically)
                )

                // Net Balance Column
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (netBalance >= 0) "+$${String.format("%.2f", netBalance)}" else "$${String.format("%.2f", netBalance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netBalance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            val progressAnim by animateFloatAsState(
                targetValue = spendPercent.toFloat().coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 800)
            )

            val progressBarColor = when {
                spendPercent >= 1.0 -> Color(0xFFEF5350)     // Red Alert
                spendPercent >= 0.85 -> Color(0xFFFFB74D)    // Amber Alert
                else -> MaterialTheme.colorScheme.primary   // Normal
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressAnim)
                        .fillMaxHeight()
                        .background(color = progressBarColor, shape = CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // State Alerts Display Check
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val alertColor = when {
                    spendPercent >= 1.0 -> Color(0xFFD32F2F)
                    spendPercent >= 0.85 -> Color(0xFFE65100)
                    else -> MaterialTheme.colorScheme.primary
                }

                val alertBg = when {
                    spendPercent >= 1.0 -> Color(0xFFFFEBEE)
                    spendPercent >= 0.85 -> Color(0xFFFFF3E0)
                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                }

                val alertMessage = when {
                    spendPercent >= 1.0 -> "Over Budget alert! You exceeded your limit by $${String.format("%.2f", totalSpent - totalBudget)}!"
                    spendPercent >= 0.85 -> "Caution: You have used ${String.format("%.1f", alertPercent)}% of your monthly budget limit."
                    else -> "Perfect! You have remaining $${String.format("%.2f", totalBudget - totalSpent)} to spend."
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = alertBg, shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (spendPercent >= 0.85) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = "Status",
                        tint = alertColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alertMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = alertColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpensesTab(
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel,
    categories: List<String>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search transactions") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_query_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Horizontal Category Filter Carousel
        ScrollableTabRow(
            selectedTabIndex = if (uiState.filterCategory == null) 0 else categories.indexOf(uiState.filterCategory) + 1,
            containerColor = Color.Transparent,
            edgePadding = 16.dp,
            indicator = { TabRowDefaults.SecondaryIndicator(color = Color.Transparent) },
            divider = {}
        ) {
            val allSelected = uiState.filterCategory == null
            val customBgAll = if (allSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val customColorAll = if (allSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .background(color = customBgAll, shape = RoundedCornerShape(16.dp))
                    .clickable { viewModel.setFilterCategory(null) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "All",
                    fontWeight = FontWeight.Bold,
                    color = customColorAll,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            categories.forEach { cat ->
                val selected = uiState.filterCategory == cat
                val customBg = if (selected) getCategoryColor(cat) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                val customColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .background(color = customBg, shape = RoundedCornerShape(16.dp))
                        .clickable { viewModel.setFilterCategory(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = customColor
                        )
                        Text(
                            text = cat,
                            fontWeight = FontWeight.Bold,
                            color = customColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // SMS Synchronization Engine Card
        val scanScope = rememberCoroutineScope()
        var scannedSmsList by remember { mutableStateOf<List<ParsedTransaction>>(emptyList()) }
        var isScanning by remember { mutableStateOf(false) }
        var showScanResults by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val smsPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                scanScope.launch {
                    isScanning = true
                    scannedSmsList = viewModel.scanDeviceSms(context)
                    isScanning = false
                    showScanResults = true
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "SMS Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart SMS Transaction Sync",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Scan bank alerts from Slice, Credit Cards, or Salary to auto-import.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Actual Scanning Button
                    Button(
                        onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_SMS
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                scanScope.launch {
                                    isScanning = true
                                    scannedSmsList = viewModel.scanDeviceSms(context)
                                    isScanning = false
                                    showScanResults = true
                                }
                            } else {
                                smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Inbox", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    // Simulation Helper Button
                    TextButton(
                        onClick = {
                            // Generate synthetic demo transactional SMS texts so they can test
                            val sampleTexts = listOf(
                                "Alert: Your account has been credited with Salary INR 75,000.00 on 24-May-2026. Ref No: 194828.",
                                "HDFC Bank: Rs. 1,450.00 spent on your Credit Card ending 8213 at AMAZON. Limit remaining: Rs. 48,000.",
                                "Slice transaction of Rs. 420.00 success at SWIGGY. Cashback credited.",
                                "Account x9210 debited by $45.90 for Uber ride on 24/05/2026.",
                                "Credited: Freecharge wallet received Rs. 200.00 from coupon."
                            )
                            val list = mutableListOf<ParsedTransaction>()
                            sampleTexts.forEach { txt ->
                                SmsTransactionParser.parseMessage(txt, System.currentTimeMillis())?.let {
                                    list.add(it)
                                }
                            }
                            scannedSmsList = list
                            showScanResults = true
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Try Demo Messages", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expanded SMS Scan results overlay
        if (showScanResults) {
            AlertDialog(
                onDismissRequest = { showScanResults = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                        Text("Parsed Financial SMS Alerts", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (scannedSmsList.isEmpty()) {
                            Text(
                                text = "No parsed credit/debit transaction messages detected in your inbox. Try clicking 'Try Demo Messages' on the card to see the scanner in action!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            Text(
                                text = "We parsed ${scannedSmsList.size} financial messages. Click 'Import' to add them to your log ledger dynamically.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            scannedSmsList.forEachIndexed { index, item ->
                                var hasImported by remember(index) { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            color = if (item.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                                            shape = CircleShape
                                                        )
                                                )
                                                Text(
                                                    text = if (item.isIncome) "+$${String.format("%.2f", item.amount)}" else "-$${String.format("%.2f", item.amount)}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (item.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = getCategoryColor(item.category),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.note,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "\"${item.rawBody}\"",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (!hasImported) {
                                                    viewModel.addExpense(
                                                        title = item.note,
                                                        amount = item.amount,
                                                        category = item.category,
                                                        dateInMillis = item.timestamp,
                                                        note = "SMS Scan: ${item.rawBody}",
                                                        isIncome = item.isIncome
                                                    )
                                                    hasImported = true
                                                }
                                            },
                                            enabled = !hasImported,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = if (hasImported) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = if (hasImported) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hasImported) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = "Import",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showScanResults = false }) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Transactions List View
        if (uiState.expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No expenses recorded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the '+' Floating Action Button to record your first transaction for this month.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.expenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onDeleteClick = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
    onDeleteClick: () -> Unit
) {
    val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getCategoryColor(expense.category).copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = getCategoryColor(expense.category)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (expense.note.isNotEmpty()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = df.format(Date(expense.dateInMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (expense.isIncome) "+$${String.format("%.2f", expense.amount)}" else "-$${String.format("%.2f", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (expense.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_expense_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChartsTab(
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel,
    categories: List<String>
) {
    if (uiState.totalSpent == 0.0 && uiState.totalIncome == 0.0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No data for visual charts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chart generation requires some recorded transactions. Record an expense or incoming money in the main tab to preview the visual insights breakdown.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // Donut Chart Card for Expenses
        if (uiState.totalSpent > 0.0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Expenses Category Share",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    CategoryDonutChart(
                        spending = uiState.categorySpending,
                        categories = categories
                    )
                }
            }
        }

        // Donut Chart Card for Income
        if (uiState.totalIncome > 0.0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Income Sources Share",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    CategoryDonutChart(
                        spending = uiState.categoryIncome,
                        categories = viewModel.incomeCategories
                    )
                }
            }
        }

        // Budget Alert & Target Comparison Card
        if (uiState.totalSpent > 0.0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Limits & Usage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    CategoryComparisonChart(
                        spending = uiState.categorySpending,
                        budgets = uiState.categoryBudgets,
                        categories = categories
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    spending: Map<String, Double>,
    categories: List<String>,
    modifier: Modifier = Modifier
) {
    val total = spending.values.sum()
    if (total == 0.0) return

    val spendingWithColors = categories.map { cat ->
        Triple(cat, spending[cat] ?: 0.0, getCategoryColor(cat))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Custom interactive visual layout ring
        Box(
            modifier = Modifier
                .size(140.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                spendingWithColors.forEach { (_, amount, color) ->
                    if (amount > 0) {
                        val sweepAngle = ((amount / total) * 360f).toFloat()
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%.0f", total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Color mapped labels with exact numbers
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            spendingWithColors.filter { it.second > 0 }.forEach { (cat, amount, color) ->
                val percent = (amount / total) * 100
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, shape = CircleShape)
                    )
                    Column {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$${String.format("%.1f", amount)} (${String.format("%.0f", percent)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryComparisonChart(
    spending: Map<String, Double>,
    budgets: Map<String, Double>,
    categories: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val anyBudgeted = categories.any { (budgets[it] ?: 0.0) > 0.0 }
        if (!anyBudgeted) {
            Text(
                text = "💡 Tip: Go to the 'Category Budgets' tab to set individual limits on food, shopping, or entertainment, and see them here!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        categories.forEach { cat ->
            val spent = spending[cat] ?: 0.0
            val limit = budgets[cat] ?: 0.0

            if (spent > 0 || limit > 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = getCategoryColor(cat)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = if (limit > 0) {
                                "$${String.format("%.0f", spent)} of $${String.format("%.0f", limit)}"
                            } else {
                                "$${String.format("%.0f", spent)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    val fillFraction = if (limit > 0) (spent / limit).toFloat() else 0.4f
                    val animatedFillFraction by animateFloatAsState(
                        targetValue = fillFraction.coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 600)
                    )

                    val barColor = when {
                        limit == 0.0 -> getCategoryColor(cat)
                        fillFraction >= 1.0 -> Color(0xFFE53935) // Red alert!
                        fillFraction >= 0.8 -> Color(0xFFFB8C00) // Caution amber
                        else -> getCategoryColor(cat)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animatedFillFraction)
                                .fillMaxHeight()
                                .background(color = barColor, shape = CircleShape)
                        )
                    }

                    // Budget threshold micro-warnings
                    if (limit > 0 && spent >= limit * 0.8) {
                        val overLimitText = if (spent >= limit) {
                            "⚠️ Budget Alert: Exceeded by $${String.format("%.0f", spent - limit)}!"
                        } else {
                            "⚠️ Warning: Over 80% used."
                        }
                        Text(
                            text = overLimitText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (spent >= limit) Color(0xFFD32F2F) else Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetsTab(
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel,
    categories: List<String>
) {
    var editCategoryName by remember { mutableStateOf<String?>(null) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Configure Category Monthly Limits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Track specific targets for high-activity categories to manage visual alerts and warn of overruns before month-end.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        categories.forEach { cat ->
            val budgetLimit = uiState.categoryBudgets[cat] ?: 0.0
            val spent = uiState.categorySpending[cat] ?: 0.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("category_budget_card_${cat}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = getCategoryColor(cat).copy(alpha = 0.12f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                tint = getCategoryColor(cat)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cat,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (budgetLimit > 0.0) {
                                    "Limit: $${String.format("%.0f", budgetLimit)} | Spent: $${String.format("%.0f", spent)}"
                                } else {
                                    "No spending limit set yet"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            editCategoryName = cat
                            showEditCategoryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("set_budget_for_${cat}")
                    ) {
                        Text(
                            text = if (budgetLimit > 0.0) "Edit" else "Set",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showEditCategoryDialog && editCategoryName != null) {
        val cat = editCategoryName!!
        val currentBudget = uiState.categoryBudgets[cat] ?: 0.0
        
        EditCategoryBudgetDialog(
            category = cat,
            currentBudget = currentBudget,
            onDismiss = {
                showEditCategoryDialog = false
                editCategoryName = null
            },
            onSave = { newLimit ->
                viewModel.setCategoryBudget(cat, newLimit)
                showEditCategoryDialog = false
                editCategoryName = null
            }
        )
    }
}

// AlertDialogs
@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Specify a reference limit that guides visual trackers and triggers caution screens across all spending categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it
                        errorText = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input"),
                    label = { Text("Total Monthly Budget Limit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    isError = errorText != null
                )
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = budgetText.toDoubleOrNull()
                    if (value == null || value <= 0) {
                        errorText = "Enter a valid budget limit greater than zero."
                    } else {
                        onSave(value)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditCategoryBudgetDialog(
    category: String,
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var limitText by remember { mutableStateOf(if (currentBudget > 0.0) currentBudget.toString() else "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Target Budget for $category", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Track customized warnings specifically for $category expenses. Enter 0 to remove constraints.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        errorText = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_budget_limit_input"),
                    label = { Text("$category Budget Limit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    isError = errorText != null
                )
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = limitText.toDoubleOrNull()
                    if (value == null) {
                        errorText = "Enter a valid numeric spending goal."
                    } else if (value < 0) {
                        errorText = "Cannot enter negative spending goals."
                    } else {
                        onSave(value)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_category_budget_button")
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddExpenseDialog(
    viewModel: ExpenseViewModel,
    categories: List<String>,
    onDismiss: () -> Unit
) {
    var isIncome by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var note by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isIncome) "Record Income" else "Record Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle between Expense and Income
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (!isIncome) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { 
                                isIncome = false 
                                selectedCategory = categories.first()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (!isIncome) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isIncome) Color(0xFF2E7D32) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { 
                                isIncome = true 
                                selectedCategory = viewModel.incomeCategories.first()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Description field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_expense_title_input"),
                    label = { Text("Topic / Description") },
                    isError = titleError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_expense_amount_input"),
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Selection Carousel (Modern Visual Grid Chips style)
                Text(
                    text = "Select Category:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                val currentCategories = if (isIncome) viewModel.incomeCategories else categories
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentCategories.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pair.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                val selectBg = if (isSelected) getCategoryColor(cat) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                val selectCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(color = selectBg, shape = RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(8.dp)
                                        .testTag("category_chip_${cat}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = selectCol
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat,
                                        color = selectCol,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Date Picker trigger Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            showDatePicker = true
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Date:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = df.format(Date(dateInMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Optional comment note field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_expense_note_input"),
                    label = { Text("Comment / Notes (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (title.isBlank()) {
                        titleError = true
                    }
                    if (amountVal == null || amountVal <= 0) {
                        amountError = true
                    }

                    if (title.isNotBlank() && amountVal != null && amountVal > 0) {
                        viewModel.addExpense(
                            title = title.trim(),
                            amount = amountVal,
                            category = selectedCategory,
                            dateInMillis = dateInMillis,
                            note = note.trim(),
                            isIncome = isIncome
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        ComposeDatePickerDialog(
            initialSelectedDateMillis = dateInMillis,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                dateInMillis = selectedDate
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeDatePickerDialog(
    initialSelectedDateMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
    M3DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun MonthYearPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onSelect: (Int, Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedYear-- }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Year"
                    )
                }
                Text(
                    text = selectedYear.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { selectedYear++ }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Year"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val months = (0..11).toList()
                val chunkedMonths = months.chunked(3)
                
                chunkedMonths.forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEach { month ->
                            val isSelected = month == currentMonth && selectedYear == currentYear
                            Button(
                                onClick = { onSelect(selectedYear, month) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = monthNames[month].take(3),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
