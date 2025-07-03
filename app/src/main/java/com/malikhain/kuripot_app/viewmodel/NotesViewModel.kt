package com.malikhain.kuripot_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import com.malikhain.kuripot_app.data.repository.NoteRepository
import com.malikhain.kuripot_app.data.repository.CategoryRepository
import com.malikhain.kuripot_app.service.AudioService
import com.malikhain.kuripot_app.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val audioService: AudioService
) : ViewModel() {
    
    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val notes: StateFlow<List<NoteEntity>> = _selectedCategoryId
        .combine(_searchQuery) { categoryId, query ->
            Pair(categoryId, query)
        }
        .flatMapLatest { (categoryId, query) ->
            when {
                query.isNotEmpty() -> noteRepository.searchNotesWithPinnedFirst(query)
                categoryId != null -> noteRepository.getNotesByCategoryWithPinnedFirst(categoryId)
                else -> noteRepository.getAllNotesWithPinnedFirst()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode
    
    private val _selectedNotes = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNotes: StateFlow<Set<Int>> = _selectedNotes
    
    private val _currentRecordingPath = MutableStateFlow<String?>(null)
    val currentRecordingPath: StateFlow<String?> = _currentRecordingPath
    
    private val _currentPlayingAudio = MutableStateFlow<String?>(null)
    val currentPlayingAudio: StateFlow<String?> = _currentPlayingAudio
    
    private val _isAudioPaused = MutableStateFlow(false)
    val isAudioPaused: StateFlow<Boolean> = _isAudioPaused
    
    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun addNote(title: String, content: String, categoryId: Int, isBudget: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d("NotesViewModel", "addNote called with title=$title, content=$content, categoryId=$categoryId, isBudget=$isBudget")
                val note = NoteEntity(
                    title = title,
                    content = content,
                    categoryId = categoryId,
                    createdAt = DateUtils.getCurrentDate(),
                    voicePath = _currentRecordingPath.value,
                    isBudget = isBudget
                )
                Log.d("NotesViewModel", "Inserting note: $note")
                val result = noteRepository.insertNote(note)
                Log.d("NotesViewModel", "Insert result: $result")
                _currentRecordingPath.value = null
                // Log notes list after insertion
                val notesList = notes.value
                Log.d("NotesViewModel", "Notes after insert: $notesList")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NotesViewModel", "Error inserting note: ${e.message}")
            }
        }
    }
    
    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }
    
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }
    
    fun restoreNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }
    
    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.togglePin(note.id, !note.isPinned)
        }
    }
    
    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            _selectedNotes.value = emptySet()
        }
    }
    
    fun toggleNoteSelection(noteId: Int) {
        val currentSelected = _selectedNotes.value.toMutableSet()
        if (currentSelected.contains(noteId)) {
            currentSelected.remove(noteId)
        } else {
            currentSelected.add(noteId)
        }
        _selectedNotes.value = currentSelected
    }
    
    fun selectAllNotes() {
        val allNoteIds = notes.value.map { it.id }.toSet()
        _selectedNotes.value = allNoteIds
    }
    
    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }
    
    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val notesToDelete = notes.value.filter { it.id in _selectedNotes.value }
            notesToDelete.forEach { note ->
                noteRepository.deleteNote(note)
            }
            _selectedNotes.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }
    
    fun archiveSelectedNotes() {
        viewModelScope.launch {
            val notesToArchive = notes.value.filter { it.id in _selectedNotes.value }
            notesToArchive.forEach { note ->
                noteRepository.deleteNote(note) // This will archive the note
            }
            _selectedNotes.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }
    
    fun addCategory(title: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(CategoryEntity(title = title))
        }
    }
    
    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
    
    fun startRecording() {
        viewModelScope.launch {
            audioService.startRecording().onSuccess { path ->
                _currentRecordingPath.value = path
                _isRecording.value = true
            }
        }
    }
    
    fun stopRecording() {
        viewModelScope.launch {
            audioService.stopRecording()
            _isRecording.value = false
        }
    }
    
    fun playAudio(audioPath: String) {
        viewModelScope.launch {
            if (_currentPlayingAudio.value == audioPath) {
                // Same audio, toggle pause/resume
                if (_isAudioPaused.value) {
                    audioService.resumePlayback()
                    _isAudioPaused.value = false
                } else {
                    audioService.pausePlayback()
                    _isAudioPaused.value = true
                }
            } else {
                // Different audio, start new playback
                audioService.startPlayback(audioPath).onSuccess {
                    _currentPlayingAudio.value = audioPath
                    _isAudioPaused.value = false
                }
            }
        }
    }
    
    fun stopAudio() {
        viewModelScope.launch {
            audioService.stopPlayback()
            _currentPlayingAudio.value = null
            _isAudioPaused.value = false
        }
    }
    
    fun pauseAudio() {
        viewModelScope.launch {
            audioService.pausePlayback()
            _isAudioPaused.value = true
        }
    }
    
    fun resumeAudio() {
        viewModelScope.launch {
            audioService.resumePlayback()
            _isAudioPaused.value = false
        }
    }
    
    fun seekAudio(position: Long) {
        audioService.seekTo(position)
    }
    
    fun clearRecording() {
        _currentRecordingPath.value?.let { path ->
            audioService.deleteAudioFile(path)
        }
        _currentRecordingPath.value = null
    }
} 