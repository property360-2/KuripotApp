package com.malikhain.kuripot_app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_entries")
data class BudgetEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val date: String,
    val description: String,
    val entryType: String, // "income" or "expense"
    val amount: Double,
    val subCategory: String?,
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null, // "weekly", "monthly", "yearly"
    val nextRecurringDate: String? = null,
    val budgetLimit: Double? = null,
    val isOverBudget: Boolean = false
) 