package com.malikhain.kuripot_app.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val notes: List<NoteExport>,
    val categories: List<CategoryExport>,
    val budget_entries: List<BudgetEntryExport>,
    val archives: List<ArchiveExport>,
    val settings: Map<String, String>
)

@Serializable
data class NoteExport(
    val id: Int,
    val title: String,
    val content: String,
    val categoryId: Int,
    val createdAt: String,
    val voicePath: String?,
    val isBudget: Boolean
)

@Serializable
data class CategoryExport(
    val id: Int,
    val title: String,
    val isDefault: Boolean
)

@Serializable
data class BudgetEntryExport(
    val id: Int,
    val noteId: Int,
    val date: String,
    val description: String,
    val entryType: String,
    val amount: Double,
    val subCategory: String?
)

@Serializable
data class ArchiveExport(
    val id: Int,
    val type: String,
    val dataJson: String,
    val deletedAt: String
) 