package com.malikhain.kuripot_app.viewmodel

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
                query.isNotEmpty() -> noteRepository.searchNotes(query)
                categoryId != null -> noteRepository.getNotesByCategory(categoryId)
                else -> noteRepository.getAllNotes()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _currentRecordingPath = MutableStateFlow<String?>(null)
    val currentRecordingPath: StateFlow<String?> = _currentRecordingPath
    
    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun addNote(title: String, content: String, categoryId: Int, isBudget: Boolean = false) {
        viewModelScope.launch {
            val note = NoteEntity(
                title = title,
                content = content,
                categoryId = categoryId,
                createdAt = DateUtils.getCurrentDate(),
                voicePath = _currentRecordingPath.value,
                isBudget = isBudget
            )
            noteRepository.insertNote(note)
            _currentRecordingPath.value = null
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
            audioService.startPlayback(audioPath)
        }
    }
    
    fun stopAudio() {
        viewModelScope.launch {
            audioService.stopPlayback()
        }
    }
    
    fun clearRecording() {
        _currentRecordingPath.value?.let { path ->
            audioService.deleteAudioFile(path)
        }
        _currentRecordingPath.value = null
    }
} 