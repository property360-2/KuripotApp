package com.malikhain.kuripot_app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val categoryId: Int,
    val createdAt: String,
    val voicePath: String?,
    val isBudget: Boolean = false,
    val isPinned: Boolean = false
) 