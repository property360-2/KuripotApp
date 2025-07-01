package com.malikhain.kuripot_app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,        // "note", "budget"
    val dataJson: String,    // Raw JSON for restoration
    val deletedAt: String
) 