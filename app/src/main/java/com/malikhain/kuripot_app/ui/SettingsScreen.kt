package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import com.malikhain.kuripot_app.viewmodel.SettingsViewModel
import com.malikhain.kuripot_app.utils.ThemeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val isPasscodeSet by viewModel.isPasscodeSet.collectAsState()
    val archives by viewModel.archives.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val showImportFilePicker by viewModel.showImportFilePicker.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val restoreStatus by viewModel.restoreStatus.collectAsState()
    
    val context = LocalContext.current
    
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var showChangePasscodeDialog by remember { mutableStateOf(false) }
    var showArchivesDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Section
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            FilterChip(
                                selected = currentTheme == ThemeUtils.THEME_LIGHT,
                                onClick = { viewModel.setTheme(ThemeUtils.THEME_LIGHT) },
                                label = { Text("Light") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = currentTheme == ThemeUtils.THEME_DARK,
                                onClick = { viewModel.setTheme(ThemeUtils.THEME_DARK) },
                                label = { Text("Dark") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = currentTheme == ThemeUtils.THEME_SYSTEM,
                                onClick = { viewModel.setTheme(ThemeUtils.THEME_SYSTEM) },
                                label = { Text("System") }
                            )
                        }
                    }
                }
            }
            
            // Security Section
            item {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Passcode",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (isPasscodeSet) "Set" else "Not set",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = { 
                                    if (isPasscodeSet) showChangePasscodeDialog = true 
                                    else showPasscodeDialog = true 
                                }
                            ) {
                                Text(if (isPasscodeSet) "Change" else "Set")
                            }
                        }
                    }
                }
            }
            
            // Data Management Section
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Archives",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Button(onClick = { showArchivesDialog = true }) {
                                Text("View (${archives.size})")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportData(context) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Export")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export")
                            }
                            
                            Button(
                                onClick = { viewModel.showImportFilePicker(context) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Import")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showBackupDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Backup")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup")
                            }
                            
                            Button(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Restore")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore")
                            }
                        }
                        
                        // Status messages
                        exportStatus?.let { status ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status.startsWith("Export failed")) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        importStatus?.let { status ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status.startsWith("Import failed")) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        backupStatus?.let { status ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status.startsWith("Backup failed")) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        restoreStatus?.let { status ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status.startsWith("Restore failed")) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showPasscodeDialog) {
        SetPasscodeDialog(
            onDismiss = { showPasscodeDialog = false },
            onSetPasscode = { passcode ->
                viewModel.setPasscode(passcode)
                showPasscodeDialog = false
            }
        )
    }
    
    if (showChangePasscodeDialog) {
        ChangePasscodeDialog(
            onDismiss = { showChangePasscodeDialog = false },
            onChangePasscode = { currentPasscode, newPasscode ->
                viewModel.changePasscode(currentPasscode, newPasscode)
                showChangePasscodeDialog = false
            }
        )
    }
    
    if (showArchivesDialog) {
        ArchivesDialog(
            archives = archives,
            onDismiss = { showArchivesDialog = false },
            onRestore = { archive -> viewModel.restoreArchive(archive) },
            onDelete = { archive -> viewModel.permanentlyDeleteArchive(archive) }
        )
    }
    
    // Import File Picker Dialog
    if (showImportFilePicker) {
        ImportFilePickerDialog(
            onDismiss = { viewModel.hideImportFilePicker() },
            onFileSelected = { filePath ->
                viewModel.importData(context, filePath)
                viewModel.hideImportFilePicker()
            }
        )
    }
    
    // Backup Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Create Backup") },
            text = { Text("Create a complete backup of all your data? This will save all notes, categories, budget entries, and settings.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createBackup(context)
                        showBackupDialog = false
                    }
                ) {
                    Text("Create Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore from Backup") },
            text = { Text("Restore all data from a backup file? This will merge the backup data with your current data.") },
            confirmButton = {
                Button(
                    onClick = {
                        // This would typically open a file picker
                        // For now, we'll use a placeholder
                        showRestoreDialog = false
                    }
                ) {
                    Text("Select Backup File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 