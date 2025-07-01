package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.viewmodel.BudgetViewModel
import com.malikhain.kuripot_app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateToNotes: () -> Unit
) {
    val budgetEntries by viewModel.budgetEntries.collectAsState()
    val budgetNotes by viewModel.budgetNotes.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedEntryType by viewModel.selectedEntryType.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val monthlyBalance by viewModel.monthlyBalance.collectAsState()
    
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<BudgetEntryEntity?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { Text("Budget Tracker") },
            actions = {
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
        Text(
            text = "Month: ${DateUtils.formatMonthYearForDisplay(selectedMonth)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
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
        
        // Budget Entries List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(budgetEntries) { entry ->
                BudgetEntryCard(
                    entry = entry,
                    onEdit = { selectedEntry = entry },
                    onDelete = { viewModel.deleteBudgetEntry(entry) }
                )
            }
        }
    }
    
    // Add Entry FAB (use Box for alignment)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAddEntryDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Entry")
        }
    }
    
    // Dialogs
    if (showAddEntryDialog) {
        AddBudgetEntryDialog(
            budgetNotes = budgetNotes,
            onDismiss = { showAddEntryDialog = false },
            onAddEntry = { noteId, description, amount, entryType, subCategory ->
                viewModel.addBudgetEntry(noteId, description, amount, entryType, subCategory)
                showAddEntryDialog = false
            }
        )
    }
    
    selectedEntry?.let { entry ->
        EditBudgetEntryDialog(
            entry = entry,
            onDismiss = { selectedEntry = null },
            onUpdateEntry = { updatedEntry ->
                viewModel.updateBudgetEntry(updatedEntry)
                selectedEntry = null
            }
        )
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