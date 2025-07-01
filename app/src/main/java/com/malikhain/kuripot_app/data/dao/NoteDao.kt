package com.malikhain.kuripot_app.data.dao

import androidx.room.*
import com.malikhain.kuripot_app.data.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getNotesByCategory(categoryId: Int): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isBudget = 1 ORDER BY createdAt DESC")
    fun getBudgetNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): NoteEntity?
    
    @Insert
    suspend fun insertNote(note: NoteEntity): Long
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
} 