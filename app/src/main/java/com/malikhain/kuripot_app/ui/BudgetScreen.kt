package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.vector.ImageVector
import com.malikhain.kuripot_app.ui.theme.PieChart
import com.malikhain.kuripot_app.ui.theme.PieChartData
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.entities.BudgetLimitEntity
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.viewmodel.BudgetViewModel
import com.malikhain.kuripot_app.utils.DateUtils
import com.malikhain.kuripot_app.ui.theme.*
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateToNotes: () -> Unit,
    context: Context
) {
    val budgetEntries by viewModel.budgetEntries.collectAsState()
    val budgetNotes by viewModel.budgetNotes.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedEntryType by viewModel.selectedEntryType.collectAsState()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState()
    val availableSubcategories by viewModel.availableSubcategories.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val monthlyBalance by viewModel.monthlyBalance.collectAsState()
    val showMonthPicker by viewModel.showMonthPicker.collectAsState()
    val expenseChartData by viewModel.expenseChartData.collectAsState()
    val incomeExpenseTrend by viewModel.incomeExpenseTrend.collectAsState()
    val budgetLimits by viewModel.budgetLimits.collectAsState()
    val overBudgetAlerts by viewModel.overBudgetAlerts.collectAsState()
    val lastDeletedEntry by viewModel.lastDeletedEntry.collectAsState()
    
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<BudgetEntryEntity?>(null) }
    var showCharts by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showBudgetLimitDialog by remember { mutableStateOf(false) }
    var showOverBudgetAlert by remember { mutableStateOf(false) }
    var showUndoSnackbar by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Load subcategories when month changes
    LaunchedEffect(selectedMonth) {
        viewModel.loadAvailableSubcategories()
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { Text("Budget Tracker") },
            actions = {
                IconButton(onClick = { showCharts = !showCharts }) {
                    Icon(Icons.Default.PieChart, contentDescription = "Charts")
                }
                IconButton(onClick = { showBudgetLimitDialog = true }) {
                    Icon(Icons.Default.Warning, contentDescription = "Budget Limits")
                }
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(Icons.Default.Download, contentDescription = "Export")
                }
                IconButton(onClick = { onNavigateToNotes() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Notes")
                }
            }
        )
        
        // Monthly Summary Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Income",
                amount = monthlyIncome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Expense",
                amount = monthlyExpense,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Balance",
                amount = monthlyBalance,
                color = if (monthlyBalance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Month Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Month: ${DateUtils.formatMonthYearForDisplay(selectedMonth)}",
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = { viewModel.showMonthPicker() }
            ) {
                Text("Change Month")
            }
        }
        
        // Charts Section
        if (showCharts) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (expenseChartData.isNotEmpty()) {
                        PieChart(
                            data = expenseChartData,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Legend
                        Column {
                            expenseChartData.forEach { data ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(data.color, shape = androidx.compose.foundation.shape.CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${data.label}: ₱${String.format("%.0f", data.value)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    } else {
                        EmptyState(
                            icon = Icons.Default.PieChart,
                            title = "No Expense Data",
                            message = "Add some expenses to see the breakdown chart"
                        )
                    }
                }
            }
        }
        
        // Budget Limits Section
        if (budgetLimits.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Budget Limits",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    budgetLimits.forEach { limit ->
                        val categoryName = "Category ${limit.categoryId}" // TODO: Get actual category name
                        BudgetLimitProgressCard(
                            limit = limit,
                            categoryName = categoryName,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Entry Type Tabs
        TabRow(
            selectedTabIndex = when (selectedEntryType) {
                "income" -> 0
                "expense" -> 1
                "all" -> 2
                else -> 0
            }
        ) {
            Tab(
                selected = selectedEntryType == "income",
                onClick = { viewModel.selectEntryType("income") },
                text = { Text("Income") }
            )
            Tab(
                selected = selectedEntryType == "expense",
                onClick = { viewModel.selectEntryType("expense") },
                text = { Text("Expense") }
            )
            Tab(
                selected = selectedEntryType == "all",
                onClick = { viewModel.selectEntryType("all") },
                text = { Text("All") }
            )
        }
        
        // Subcategory Filter
        if (availableSubcategories.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filter by Subcategory",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSubcategory == null,
                                onClick = { viewModel.selectSubcategory(null) },
                                label = { Text("All") }
                            )
                        }
                        items(availableSubcategories) { subcategory ->
                            FilterChip(
                                selected = selectedSubcategory == subcategory,
                                onClick = { viewModel.selectSubcategory(subcategory) },
                                label = { Text(subcategory) }
                            )
                        }
                    }
                }
            }
        }
        
        // Budget Entries List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (budgetEntries.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        title = "No Budget Entries",
                        message = when {
                            selectedEntryType != "all" -> "No ${selectedEntryType} entries for this month"
                            selectedSubcategory != null -> "No entries for ${selectedSubcategory}"
                            else -> "Add your first budget entry to get started"
                        }
                    )
                }
            } else {
                // Show recurring entries first
                val recurringEntries = budgetEntries.filter { it.isRecurring }
                if (recurringEntries.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recurring Entries",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(recurringEntries) { entry ->
                        RecurringEntryCard(
                            entry = entry,
                            onEdit = { selectedEntry = entry },
                            onDelete = { 
                                viewModel.deleteBudgetEntry(entry)
                                showUndoSnackbar = true
                            }
                        )
                    }
                    
                    item {
                        Text(
                            text = "All Entries",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                items(budgetEntries.filter { !it.isRecurring }) { entry ->
                    BudgetEntryCard(
                        entry = entry,
                        onEdit = { selectedEntry = entry },
                        onDelete = { 
                            viewModel.deleteBudgetEntry(entry)
                            showUndoSnackbar = true
                        }
                    )
                }
            }
        }
    }
    
    // Add Entry FAB
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAddEntryDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Entry")
        }
    }
    
    // Undo Snackbar
    if (showUndoSnackbar && lastDeletedEntry != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(
                    onClick = {
                        viewModel.undoDelete()
                        showUndoSnackbar = false
                    }
                ) {
                    Text("UNDO")
                }
            },
            onDismiss = {
                viewModel.clearLastDeletedEntry()
                showUndoSnackbar = false
            }
        ) {
            Text("Budget entry deleted")
        }
    }
    
    // Error Snackbar
    if (showErrorSnackbar) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(
                    onClick = { showErrorSnackbar = false }
                ) {
                    Text("DISMISS")
                }
            },
            onDismiss = { showErrorSnackbar = false }
        ) {
            Text(errorMessage)
        }
    }
    
    // Dialogs
    if (showAddEntryDialog) {
        AddBudgetEntryDialog(
            budgetNotes = budgetNotes,
            onDismiss = { showAddEntryDialog = false },
            onAddEntry = { noteId, description, amount, entryType, subCategory, isRecurring, recurringFrequency ->
                try {
                    viewModel.addBudgetEntry(noteId.toIntOrNull() ?: 0, description, amount, entryType, subCategory, isRecurring, recurringFrequency)
                    showAddEntryDialog = false
                } catch (e: Exception) {
                    errorMessage = "Failed to add entry: ${e.message}"
                    showErrorSnackbar = true
                }
            }
        )
    }
    
    selectedEntry?.let { entry ->
        EditBudgetEntryDialog(
            entry = entry,
            onDismiss = { selectedEntry = null },
            onUpdateEntry = { updatedEntry ->
                try {
                    viewModel.updateBudgetEntry(updatedEntry)
                    selectedEntry = null
                } catch (e: Exception) {
                    errorMessage = "Failed to update entry: ${e.message}"
                    showErrorSnackbar = true
                }
            }
        )
    }
    
    // Month Picker Dialog
    if (showMonthPicker) {
        MonthPickerDialog(
            currentMonth = selectedMonth,
            availableMonths = viewModel.getAvailableMonths(),
            onDismiss = { viewModel.hideMonthPicker() },
            onMonthSelected = { month ->
                viewModel.selectMonth(month)
                viewModel.hideMonthPicker()
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Budget Data") },
            text = { Text("Export current month's budget data to CSV file?") },
            confirmButton = {
                Button(
                    onClick = {
                        val result = viewModel.exportToCSV(context)
                        result.fold(
                            onSuccess = { filePath ->
                                showExportDialog = false
                                // Show success message
                            },
                            onFailure = { exception ->
                                showExportDialog = false
                                errorMessage = "Export failed: ${exception.message}"
                                showErrorSnackbar = true
                            }
                        )
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Budget Limit Dialog
    if (showBudgetLimitDialog) {
        BudgetLimitDialog(
            categories = emptyList(), // TODO: Get categories from ViewModel
            onDismiss = { showBudgetLimitDialog = false },
            onSetLimit = { categoryId, limit ->
                try {
                    viewModel.setBudgetLimit(categoryId, viewModel.selectedMonth.value, limit)
                    showBudgetLimitDialog = false
                } catch (e: Exception) {
                    errorMessage = "Failed to set budget limit: ${e.message}"
                    showErrorSnackbar = true
                }
            }
        )
    }
    
    // Over Budget Alert Dialog
    if (showOverBudgetAlert && overBudgetAlerts.isNotEmpty()) {
        OverBudgetAlertDialog(
            overBudgetLimits = overBudgetAlerts,
            categories = emptyList(), // TODO: Get categories from ViewModel
            onDismiss = { showOverBudgetAlert = false }
        )
    }
    
    // Auto-show over budget alert when alerts are present
    LaunchedEffect(overBudgetAlerts) {
        if (overBudgetAlerts.isNotEmpty() && !showOverBudgetAlert) {
            showOverBudgetAlert = true
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "₱${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEntryCard(
    entry: BudgetEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = DateUtils.formatDateForDisplay(entry.date),
                    style = MaterialTheme.typography.bodySmall
                )
                entry.subCategory?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₱${String.format("%.2f", entry.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (entry.entryType == "income") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetEntryDialog(
    budgetNotes: List<NoteEntity>,
    onDismiss: () -> Unit,
    onAddEntry: (String, String, Double, String, String, Boolean, String?) -> Unit
) {
    var noteId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var entryType by remember { mutableStateOf("income") }
    var subCategory by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringFrequency by remember { mutableStateOf("monthly") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget Entry") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Entry Type Selection
                Text(
                    text = "Entry Type",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = entryType == "income",
                        onClick = { entryType = "income" },
                        label = { Text("Income") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = entryType == "expense",
                        onClick = { entryType = "expense" },
                        label = { Text("Expense") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₱") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subcategory
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = { Text("Subcategory (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recurring Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Text(
                        text = "Make this recurring",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("weekly", "monthly", "yearly").forEach { freq ->
                            FilterChip(
                                selected = recurringFrequency == freq,
                                onClick = { recurringFrequency = freq },
                                label = { Text(freq.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let { amountValue ->
                        onAddEntry(
                            noteId,
                            description,
                            amountValue,
                            entryType,
                            subCategory,
                            isRecurring,
                            if (isRecurring) recurringFrequency else null
                        )
                    }
                },
                enabled = description.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text("Add Entry")
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
fun BudgetLimitProgressCard(
    limit: BudgetLimitEntity,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    val progress = if (limit.limit > 0) (limit.spent / limit.limit).coerceIn(0.0, 1.0) else 0.0
    val isOverBudget = limit.spent > limit.limit
    val progressColor = when {
        isOverBudget -> MaterialTheme.colorScheme.error
        progress > 0.8 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (isOverBudget) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Over Budget",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.toFloat())
                        .fillMaxHeight()
                        .background(
                            progressColor,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₱${String.format("%.0f", limit.spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "₱${String.format("%.0f", limit.limit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isOverBudget) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Over budget by ₱${String.format("%.0f", limit.spent - limit.limit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RecurringEntryCard(
    entry: BudgetEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Recurring",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${entry.recurringFrequency?.replaceFirstChar { it.uppercase() }} • Next: ${entry.nextRecurringDate ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "₱${String.format("%.2f", entry.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (entry.entryType == "income") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun OverBudgetAlertDialog(
    overBudgetLimits: List<BudgetLimitEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Budget Alerts")
            }
        },
        text = {
            Column {
                Text(
                    text = "The following categories are over budget:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                overBudgetLimits.forEach { limit ->
                    val category = categories.find { it.id == limit.categoryId }
                    Text(
                        text = "• ${category?.title ?: "Unknown"}: ₱${String.format("%.0f", limit.spent)} / ₱${String.format("%.0f", limit.limit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun BudgetLimitDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSetLimit: (Int, Double) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var limitAmount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget Limit") },
        text = {
            Column {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.height(120.dp)
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategoryId = category.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id }
                            )
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = limitAmount,
                    onValueChange = { limitAmount = it },
                    label = { Text("Monthly Limit (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategoryId?.let { categoryId ->
                        limitAmount.toDoubleOrNull()?.let { amount ->
                            onSetLimit(categoryId, amount)
                            onDismiss()
                        }
                    }
                },
                enabled = selectedCategoryId != null && limitAmount.isNotEmpty()
            ) {
                Text("Set Limit")
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
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
} 