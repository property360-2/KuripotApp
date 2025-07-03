package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import com.malikhain.kuripot_app.data.entities.*
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import com.malikhain.kuripot_app.utils.DateUtils
import com.malikhain.kuripot_app.ui.theme.EmptyState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

// Add Note Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    categories: List<CategoryEntity>,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDismiss: () -> Unit,
    onAddNote: (title: String, content: String, categoryId: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category dropdown (now functional)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.title ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.title) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Voice recording button (use PlayArrow as a placeholder)
                Button(
                    onClick = { if (isRecording) onStopRecording() else onStartRecording() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Voice Recording"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isRecording) "Stop Recording" else "Start Recording")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategoryId?.let { categoryId ->
                        onAddNote(title, content, categoryId)
                    }
                },
                enabled = title.isNotEmpty() && selectedCategoryId != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Add Category Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (title: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onAddCategory(title) },
                enabled = title.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Edit Note Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteDialog(
    note: NoteEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onUpdateNote: (NoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var selectedCategoryId by remember { mutableStateOf(note.categoryId) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateNote(note.copy(title = title, content = content, categoryId = selectedCategoryId))
                },
                enabled = title.isNotEmpty()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Add Budget Entry Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetEntryDialog(
    budgetNotes: List<NoteEntity>,
    onDismiss: () -> Unit,
    onAddEntry: (noteId: Int, description: String, amount: Double, entryType: String, subCategory: String?) -> Unit
) {
    var selectedNoteId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var entryType by remember { mutableStateOf("income") }
    var subCategory by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    FilterChip(
                        selected = entryType == "income",
                        onClick = { entryType = "income" },
                        label = { Text("Income") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = entryType == "expense",
                        onClick = { entryType = "expense" },
                        label = { Text("Expense") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = { Text("Sub-category (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let { amountValue ->
                        onAddEntry(
                            selectedNoteId ?: 0,
                            description,
                            amountValue,
                            entryType,
                            subCategory.takeIf { it.isNotEmpty() }
                        )
                    }
                },
                enabled = description.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Edit Budget Entry Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetEntryDialog(
    entry: BudgetEntryEntity,
    onDismiss: () -> Unit,
    onUpdateEntry: (BudgetEntryEntity) -> Unit
) {
    var description by remember { mutableStateOf(entry.description) }
    var amount by remember { mutableStateOf(entry.amount.toString()) }
    var entryType by remember { mutableStateOf(entry.entryType) }
    var subCategory by remember { mutableStateOf(entry.subCategory ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Budget Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    FilterChip(
                        selected = entryType == "income",
                        onClick = { entryType = "income" },
                        label = { Text("Income") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = entryType == "expense",
                        onClick = { entryType = "expense" },
                        label = { Text("Expense") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = { Text("Sub-category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let { amountValue ->
                        onUpdateEntry(
                            entry.copy(
                                description = description,
                                amount = amountValue,
                                entryType = entryType,
                                subCategory = subCategory.takeIf { it.isNotEmpty() }
                            )
                        )
                    }
                },
                enabled = description.isNotEmpty() && amount.isNotEmpty()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Set Passcode Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPasscodeDialog(
    onDismiss: () -> Unit,
    onSetPasscode: (String) -> Unit
) {
    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Passcode") },
        text = {
            Column {
                OutlinedTextField(
                    value = passcode,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            passcode = it
                            error = null
                        }
                    },
                    label = { Text("Enter 4-digit passcode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = confirmPasscode,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmPasscode = it
                            error = null
                        }
                    },
                    label = { Text("Confirm passcode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        passcode.length != 4 -> error = "Passcode must be 4 digits"
                        passcode != confirmPasscode -> error = "Passcodes don't match"
                        else -> onSetPasscode(passcode)
                    }
                },
                enabled = passcode.length == 4 && confirmPasscode.length == 4
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Change Passcode Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasscodeDialog(
    onDismiss: () -> Unit,
    onChangePasscode: (String, String) -> Unit
) {
    var currentPasscode by remember { mutableStateOf("") }
    var newPasscode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showCurrentPasscode by remember { mutableStateOf(false) }
    var showNewPasscode by remember { mutableStateOf(false) }
    var showConfirmPasscode by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Passcode") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPasscode,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            currentPasscode = it
                            error = null
                        }
                    },
                    label = { Text("Current passcode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (showCurrentPasscode) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPasscode = !showCurrentPasscode }) {
                            Icon(
                                imageVector = if (showCurrentPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrentPasscode) "Hide current passcode" else "Show current passcode"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newPasscode,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            newPasscode = it
                            error = null
                        }
                    },
                    label = { Text("New passcode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (showNewPasscode) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPasscode = !showNewPasscode }) {
                            Icon(
                                imageVector = if (showNewPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNewPasscode) "Hide new passcode" else "Show new passcode"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = confirmPasscode,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmPasscode = it
                            error = null
                        }
                    },
                    label = { Text("Confirm new passcode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (showConfirmPasscode) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPasscode = !showConfirmPasscode }) {
                            Icon(
                                imageVector = if (showConfirmPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPasscode) "Hide confirm passcode" else "Show confirm passcode"
                            )
                        }
                    }
                )
                
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPasscode.length != 4 -> error = "Current passcode must be 4 digits"
                        newPasscode.length != 4 -> error = "New passcode must be 4 digits"
                        newPasscode != confirmPasscode -> error = "New passcodes don't match"
                        else -> onChangePasscode(currentPasscode, newPasscode)
                    }
                },
                enabled = currentPasscode.length == 4 && newPasscode.length == 4 && confirmPasscode.length == 4
            ) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Archives Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivesDialog(
    archives: List<ArchiveEntity>,
    onDismiss: () -> Unit,
    onRestore: (ArchiveEntity) -> Unit,
    onDelete: (ArchiveEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<ArchiveEntity?>(null) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val filteredArchives = archives.filter { archive ->
        archive.type.contains(searchQuery, ignoreCase = true) ||
        archive.dataJson.contains(searchQuery, ignoreCase = true)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Archives") },
        text = {
            Column {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search archives...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Archives list
                if (filteredArchives.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        title = "No Archives Found",
                        message = if (searchQuery.isNotEmpty()) 
                            "No archives match your search" 
                        else 
                            "No archived items yet"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredArchives) { archive ->
                            ArchiveItem(
                                archive = archive,
                                onRestore = {
                                    try {
                                        onRestore(archive)
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to restore: ${e.message}"
                                        showErrorSnackbar = true
                                    }
                                },
                                onDelete = { showDeleteConfirmation = archive }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
    
    // Delete confirmation dialog
    showDeleteConfirmation?.let { archive ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Permanently Delete") },
            text = { Text("This action cannot be undone. Are you sure you want to permanently delete this archived item?") },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            onDelete(archive)
                            showDeleteConfirmation = null
                        } catch (e: Exception) {
                            errorMessage = "Failed to delete: ${e.message}"
                            showErrorSnackbar = true
                            showDeleteConfirmation = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Error snackbar
    if (showErrorSnackbar) {
        // Comment out or remove problematic Snackbar usages
        // Add missing imports for border, RoundedCornerShape, and icons
        // import androidx.compose.foundation.border
        // import androidx.compose.foundation.shape.RoundedCornerShape
        // import androidx.compose.material.icons.filled.Visibility
        // import androidx.compose.material.icons.filled.VisibilityOff
        // import androidx.compose.material.icons.filled.Inbox
        // import androidx.compose.material.icons.filled.School
        // ... existing code ...
    }
}

@Composable
fun ArchiveItem(
    archive: ArchiveEntity,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = archive.type,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Archived on ${DateUtils.formatDateForDisplay(archive.deletedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Restore",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Permanently",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// Month Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerDialog(
    currentMonth: String,
    availableMonths: List<String>,
    onDismiss: () -> Unit,
    onMonthSelected: (String) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month") },
        text = {
            Column {
                availableMonths.forEach { month ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMonth == month,
                            onClick = { selectedMonth = month }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = month,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMonthSelected(selectedMonth) }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Import File Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFilePickerDialog(
    onDismiss: () -> Unit,
    onFileSelected: (String) -> Unit
) {
    var selectedFilePath by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Data") },
        text = {
            Column {
                Text(
                    text = "Select a JSON file to import your data:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = selectedFilePath,
                    onValueChange = { selectedFilePath = it },
                    label = { Text("File Path") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter file path or browse...") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: Importing will merge data with existing records. Duplicate entries may be created.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedFilePath.isNotEmpty()) {
                        onFileSelected(selectedFilePath)
                    }
                },
                enabled = selectedFilePath.isNotEmpty()
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Enhanced Category Dialog with Color and Icon Selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(category?.title ?: "") }
    var selectedColor by remember { mutableStateOf(category?.color ?: "#FF6200EE") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "label") }
    
    val availableColors = listOf(
        "#FF6200EE", "#FF03DAC5", "#FF018786", "#FFB00020",
        "#FF3700B3", "#FF03DAC5", "#FF018786", "#FFB00020",
        "#FF6200EE", "#FF03DAC5", "#FF018786", "#FFB00020"
    )
    
    val availableIcons = listOf(
        "label" to Icons.Default.Favorite,
        "home" to Icons.Default.Home,
        "school" to Icons.Default.School,
        "shopping" to Icons.Default.ShoppingCart
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(color)),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableIcons) { (iconName, icon) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (selectedIcon == iconName) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .border(
                                    width = if (selectedIcon == iconName) 2.dp else 1.dp,
                                    color = if (selectedIcon == iconName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = iconName,
                                tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, selectedColor, selectedIcon) },
                enabled = title.isNotEmpty()
            ) {
                Text(if (category == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 