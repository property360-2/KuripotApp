package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.viewmodel.NotesViewModel
import com.malikhain.kuripot_app.utils.DateUtils
import com.malikhain.kuripot_app.ui.theme.AudioPlayer
import com.malikhain.kuripot_app.ui.theme.EmptyState
import androidx.compose.ui.text.input.KeyboardType
import android.util.Log

// Helper function to highlight search terms
private fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isEmpty()) {
        return AnnotatedString(text)
    }
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0
        while (true) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            }
            append(text.substring(startIndex, index))
            withStyle(style = androidx.compose.ui.text.SpanStyle(
                background = androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.5f)
            )) {
                append(text.substring(index, index + query.length))
            }
            startIndex = index + query.length
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNavigateToBudget: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val currentPlayingAudio by viewModel.currentPlayingAudio.collectAsState()
    val isAudioPaused by viewModel.isAudioPaused.collectAsState()
    
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<NoteEntity?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }
    var lastDeletedNote by remember { mutableStateOf<NoteEntity?>(null) }
    var showBatchActionDialog by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Log notes list on recomposition
    LaunchedEffect(notes) {
        Log.d("NotesScreen", "Composed notes: $notes")
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Debug: Show raw notes list
        Text(
            text = "DEBUG: Notes in ViewModel: " + notes.joinToString { "[id=${it.id}, title=${it.title}, catId=${it.categoryId}]" },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp)
        )
        // Top Bar
        TopAppBar(
            title = { Text("Notes") },
            actions = {
                if (isMultiSelectMode) {
                    IconButton(onClick = { viewModel.selectAllNotes() }) {
                        Icon(Icons.Default.CheckBox, contentDescription = "Select All")
                    }
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                    }
                    IconButton(onClick = { showBatchActionDialog = true }) {
                        Text("${selectedNotes.size}")
                    }
                } else {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Add Category")
                    }
                    IconButton(onClick = { onNavigateToBudget() }) {
                        Icon(Icons.Default.Add, contentDescription = "Budget")
                    }
                }
                IconButton(onClick = { viewModel.toggleMultiSelectMode() }) {
                    Text(if (isMultiSelectMode) "Cancel" else "Select")
                }
            }
        )
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search notes...") },
            singleLine = true
        )
        
        // Category Filter
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("All") }
                )
            }
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { viewModel.selectCategory(category.id) },
                    label = { Text(category.title) }
                )
            }
        }
        
        // Notes List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (notes.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        title = "No Notes Found",
                        message = when {
                            searchQuery.isNotEmpty() -> "No notes match your search"
                            selectedCategoryId != null -> "No notes in this category"
                            else -> "Create your first note to get started"
                        }
                    )
                    // Fallback: Show raw notes list
                    Text(
                        text = "DEBUG (LazyColumn): " + notes.joinToString { "[id=${it.id}, title=${it.title}, catId=${it.categoryId}]" },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(notes) { note ->
                    // Fallback: Show note info as text for debugging
                    Text(
                        text = "DEBUG (Item): id=${note.id}, title=${note.title}, catId=${note.categoryId}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(4.dp)
                    )
                    // NoteCard(
                    //     note = note,
                    //     category = categories.find { it.id == note.categoryId },
                    //     onEdit = { selectedNote = note },
                    //     onDelete = { 
                    //         lastDeletedNote = note
                    //         viewModel.deleteNote(note)
                    //         showUndoSnackbar = true
                    //     },
                    //     onPlayAudio = { note.voicePath?.let { viewModel.playAudio(it) } },
                    //     onTogglePin = { viewModel.togglePin(note) },
                    //     isMultiSelectMode = isMultiSelectMode,
                    //     isSelected = selectedNotes.contains(note.id),
                    //     onToggleSelection = { viewModel.toggleNoteSelection(note.id) },
                    //     searchQuery = searchQuery,
                    //     currentPlayingAudio = currentPlayingAudio,
                    //     isAudioPaused = isAudioPaused,
                    //     viewModel = viewModel
                    // )
                }
            }
        }
        
        // Add Note FAB
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }
    
    // SnackbarHost for showing snackbars
    SnackbarHost(
        hostState = remember { SnackbarHostState() },
        modifier = Modifier.padding(16.dp)
    ) {
        // Undo Snackbar
        if (showUndoSnackbar) {
            Snackbar(
                action = {
                    TextButton(
                        onClick = {
                            lastDeletedNote?.let { note ->
                                viewModel.restoreNote(note)
                            }
                            showUndoSnackbar = false
                            lastDeletedNote = null
                        }
                    ) {
                        Text("UNDO")
                    }
                }
            ) {
                Text("Note deleted")
            }
        }
        
        // Error Snackbar
        if (showErrorSnackbar) {
            Snackbar(
                action = {
                    TextButton(
                        onClick = { showErrorSnackbar = false }
                    ) {
                        Text("DISMISS")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
    
    // Batch Action Dialog
    if (showBatchActionDialog) {
        AlertDialog(
            onDismissRequest = { showBatchActionDialog = false },
            title = { Text("Batch Actions") },
            text = { Text("${selectedNotes.size} notes selected") },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            try {
                                viewModel.deleteSelectedNotes()
                                showBatchActionDialog = false
                            } catch (e: Exception) {
                                errorMessage = "Failed to delete notes: ${e.message}"
                                showErrorSnackbar = true
                                showBatchActionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Selected")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            try {
                                viewModel.archiveSelectedNotes()
                                showBatchActionDialog = false
                            } catch (e: Exception) {
                                errorMessage = "Failed to archive notes: ${e.message}"
                                showErrorSnackbar = true
                                showBatchActionDialog = false
                            }
                        }
                    ) {
                        Text("Archive Selected")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Dialogs
    if (showAddNoteDialog) {
        AddNoteDialog(
            categories = categories,
            isRecording = isRecording,
            onStartRecording = { viewModel.startRecording() },
            onStopRecording = { viewModel.stopRecording() },
            onDismiss = { showAddNoteDialog = false },
            onAddNote = { title, content, categoryId ->
                try {
                    viewModel.addNote(title, content, categoryId)
                    showAddNoteDialog = false
                } catch (e: Exception) {
                    errorMessage = "Failed to add note: ${e.message}"
                    showErrorSnackbar = true
                }
            }
        )
    }
    
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { title ->
                try {
                    viewModel.addCategory(title)
                    showAddCategoryDialog = false
                } catch (e: Exception) {
                    errorMessage = "Failed to add category: ${e.message}"
                    showErrorSnackbar = true
                }
            }
        )
    }
    
    selectedNote?.let { note ->
        EditNoteDialog(
            note = note,
            categories = categories,
            onDismiss = { selectedNote = null },
            onUpdateNote = { updatedNote ->
                try {
                    viewModel.updateNote(updatedNote)
                    selectedNote = null
                } catch (e: Exception) {
                    errorMessage = "Failed to update note: ${e.message}"
                    showErrorSnackbar = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    category: CategoryEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPlayAudio: () -> Unit,
    onTogglePin: () -> Unit,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    searchQuery: String = "",
    currentPlayingAudio: String? = null,
    isAudioPaused: Boolean = false,
    viewModel: NotesViewModel? = null
) {
    Log.d("NoteCard", "Rendering NoteCard for note id=${note.id}, title=${note.title}, catId=${note.categoryId}")
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                note.isPinned -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Log.d("NoteCard", "Showing title: ${note.title}")
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium
            )
            Log.d("NoteCard", "Showing category")
            if (category != null) {
                Text(
                    text = "Category: ${category.title}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "[No Category] (catId=${note.categoryId})",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 