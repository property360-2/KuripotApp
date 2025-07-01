package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.data.entities.*
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.data.entities.ArchiveEntity

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
                
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.title ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
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
                    label = { Text("Confirm new passcode") },
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Archives") },
        text = {
            if (archives.isEmpty()) {
                Text("No archived items")
            } else {
                Column {
                    archives.forEach { archive ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
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
                                        text = "Type: ${archive.type}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Deleted: ${archive.deletedAt}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onRestore(archive) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Restore")
                                    }
                                    IconButton(onClick = { onDelete(archive) }) {
                                        Icon(Icons.Default.Delete, "Delete")
                                    }
                                }
                            }
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
} 