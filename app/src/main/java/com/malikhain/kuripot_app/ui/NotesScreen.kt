package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.viewmodel.NotesViewModel
import com.malikhain.kuripot_app.utils.DateUtils
import com.malikhain.kuripot_app.ui.theme.AudioPlayer

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
            
            // Add text before the match
            append(text.substring(startIndex, index))
            
            // Add highlighted match
            withStyle(androidx.compose.ui.text.SpanStyle(
                backgroundColor = androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.5f)
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
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { Text("Notes") },
            actions = {
                if (isMultiSelectMode) {
                    IconButton(onClick = { viewModel.selectAllNotes() }) {
                        Icon(Icons.Default.SelectAll, contentDescription = "Select All")
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
                }
            } else {
                items(notes) { note ->
                    SwipeableNoteCard(
                        note = note,
                        category = categories.find { it.id == note.categoryId },
                        onEdit = { selectedNote = note },
                        onDelete = { 
                            lastDeletedNote = note
                            viewModel.deleteNote(note)
                            showUndoSnackbar = true
                        },
                        onPlayAudio = { note.voicePath?.let { viewModel.playAudio(it) } },
                        onTogglePin = { viewModel.togglePin(note) },
                        isMultiSelectMode = isMultiSelectMode,
                        isSelected = selectedNotes.contains(note.id),
                        onToggleSelection = { viewModel.toggleNoteSelection(note.id) },
                        searchQuery = searchQuery
                    )
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
    
    // Undo Snackbar
    if (showUndoSnackbar) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
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
            },
            onDismiss = {
                showUndoSnackbar = false
                lastDeletedNote = null
            }
        ) {
            Text("Note deleted")
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableNoteCard(
    note: NoteEntity,
    category: CategoryEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPlayAudio: () -> Unit,
    onTogglePin: () -> Unit,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    searchQuery: String = ""
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismiss(
        state = dismissState,
        background = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> MaterialTheme.colorScheme.surface
                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.error
                    DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.error
                }
            )
            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
                null -> Alignment.Center
            }
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Delete
                DismissDirection.EndToStart -> Icons.Default.Delete
                null -> null
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        },
        dismissContent = {
            NoteCard(
                note = note,
                category = category,
                onEdit = onEdit,
                onDelete = onDelete,
                onPlayAudio = onPlayAudio,
                onTogglePin = onTogglePin,
                isMultiSelectMode = isMultiSelectMode,
                isSelected = isSelected,
                onToggleSelection = onToggleSelection,
                searchQuery = searchQuery
            )
        },
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart)
    )
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
    searchQuery: String = ""
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMultiSelectMode) {
                    IconButton(onClick = onToggleSelection) {
                        Icon(
                            if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (isSelected) "Deselect" else "Select",
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (note.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = highlightText(note.title, searchQuery),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    if (!isMultiSelectMode) {
                        if (note.voicePath != null) {
                            IconButton(onClick = onPlayAudio) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio")
                            }
                        }
                        IconButton(onClick = onTogglePin) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = if (note.isPinned) "Unpin" else "Pin",
                                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
            Text(
                text = highlightText(note.content, searchQuery),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // Audio player for voice notes
            if (note.voicePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isThisAudioPlaying = currentPlayingAudio == note.voicePath
                if (isThisAudioPlaying) {
                    AudioPlayer(
                        isPlaying = isThisAudioPlaying,
                        isPaused = isAudioPaused,
                        currentPosition = 0L, // TODO: Get from AudioService
                        duration = 0L, // TODO: Get from AudioService
                        onPlay = { viewModel.resumeAudio() },
                        onPause = { viewModel.pauseAudio() },
                        onStop = { viewModel.stopAudio() },
                        onSeek = { position -> viewModel.seekAudio(position) }
                    )
                } else {
                    Button(
                        onClick = { viewModel.playAudio(note.voicePath) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Voice Note")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play Voice Note")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                category?.let {
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text(it.title) }
                    )
                }
                Text(
                    text = DateUtils.formatDateForDisplay(note.createdAt),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
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