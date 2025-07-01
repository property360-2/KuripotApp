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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.viewmodel.NotesViewModel
import com.malikhain.kuripot_app.utils.DateUtils

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
    
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<NoteEntity?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { Text("Notes") },
            actions = {
                IconButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Add Category")
                }
                IconButton(onClick = { onNavigateToBudget() }) {
                    Icon(Icons.Default.Add, contentDescription = "Budget")
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
            items(notes) { note ->
                NoteCard(
                    note = note,
                    category = categories.find { it.id == note.categoryId },
                    onEdit = { selectedNote = note },
                    onDelete = { viewModel.deleteNote(note) },
                    onPlayAudio = { note.voicePath?.let { viewModel.playAudio(it) } }
                )
            }
        }
        // Add Note FAB (use Box for alignment)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
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
                viewModel.addNote(title, content, categoryId)
                showAddNoteDialog = false
            }
        )
    }
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { title ->
                viewModel.addCategory(title)
                showAddCategoryDialog = false
            }
        )
    }
    selectedNote?.let { note ->
        EditNoteDialog(
            note = note,
            categories = categories,
            onDismiss = { selectedNote = null },
            onUpdateNote = { updatedNote ->
                viewModel.updateNote(updatedNote)
                selectedNote = null
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
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    if (note.voicePath != null) {
                        IconButton(onClick = onPlayAudio) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio")
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
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