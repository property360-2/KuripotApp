package com.malikhain.kuripot_app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
) 