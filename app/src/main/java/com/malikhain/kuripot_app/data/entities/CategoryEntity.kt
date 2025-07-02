package com.malikhain.kuripot_app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isDefault: Boolean = false,
    val color: String = "#FF6200EE", // Default Material Design primary color
    val icon: String = "label", // Default icon name
    val sortOrder: Int = 0
) 