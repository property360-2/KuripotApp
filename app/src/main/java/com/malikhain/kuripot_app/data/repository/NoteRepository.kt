package com.malikhain.kuripot_app.data.repository

import com.malikhain.kuripot_app.data.dao.NoteDao
import com.malikhain.kuripot_app.data.dao.ArchiveDao
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import com.malikhain.kuripot_app.model.NoteExport
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.malikhain.kuripot_app.utils.DateUtils

class NoteRepository(
    private val noteDao: NoteDao,
    private val archiveDao: ArchiveDao
) {
    
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    fun getNotesByCategory(categoryId: Int): Flow<List<NoteEntity>> = 
        noteDao.getNotesByCategory(categoryId)
    
    fun getBudgetNotes(): Flow<List<NoteEntity>> = noteDao.getBudgetNotes()
    
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
    
    fun getAllNotesWithPinnedFirst(): Flow<List<NoteEntity>> = noteDao.getAllNotesWithPinnedFirst()
    
    fun getNotesByCategoryWithPinnedFirst(categoryId: Int): Flow<List<NoteEntity>> = 
        noteDao.getNotesByCategoryWithPinnedFirst(categoryId)
    
    fun searchNotesWithPinnedFirst(query: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesWithPinnedFirst(query)
    
    suspend fun getNoteById(id: Int): NoteEntity? = noteDao.getNoteById(id)
    
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) {
        // Archive the note before deleting
        val noteExport = NoteExport(
            id = note.id,
            title = note.title,
            content = note.content,
            categoryId = note.categoryId,
            createdAt = note.createdAt,
            voicePath = note.voicePath,
            isBudget = note.isBudget
        )
        
        val archive = ArchiveEntity(
            type = "note",
            dataJson = Json.encodeToString(noteExport),
            deletedAt = DateUtils.getCurrentDate()
        )
        
        archiveDao.insertArchive(archive)
        noteDao.deleteNote(note)
    }
    
    suspend fun togglePin(noteId: Int, isPinned: Boolean) = noteDao.togglePin(noteId, isPinned)
} 