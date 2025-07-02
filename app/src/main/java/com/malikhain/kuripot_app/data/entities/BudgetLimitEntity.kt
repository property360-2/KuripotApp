package com.malikhain.kuripot_app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_limits")
data class BudgetLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val month: String, // Format: "YYYY-MM"
    val limit: Double,
    val spent: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
) 