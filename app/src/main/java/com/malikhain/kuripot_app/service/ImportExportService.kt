package com.malikhain.kuripot_app.service

import android.content.Context
import com.malikhain.kuripot_app.data.entities.*
import com.malikhain.kuripot_app.data.repository.*
import com.malikhain.kuripot_app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImportExportService(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val archiveRepository: ArchiveRepository,
    private val settingRepository: SettingRepository
) {
    
    suspend fun exportData(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Collect all data
            val notes = noteRepository.getAllNotes().first()
            val categories = categoryRepository.getAllCategories().first()
            val budgetEntries = budgetRepository.getBudgetEntriesByMonth("").first()
            val archives = archiveRepository.getAllArchives().first()
            val settings = settingRepository.getAllSettings().first()
            
            // Convert to export format
            val exportData = ExportData(
                notes = notes.map { note: NoteEntity ->
                    NoteExport(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        categoryId = note.categoryId,
                        createdAt = note.createdAt,
                        voicePath = note.voicePath,
                        isBudget = note.isBudget
                    )
                },
                categories = categories.map { category: CategoryEntity ->
                    CategoryExport(
                        id = category.id,
                        title = category.title,
                        isDefault = category.isDefault
                    )
                },
                budget_entries = budgetEntries.map { entry: BudgetEntryEntity ->
                    BudgetEntryExport(
                        id = entry.id,
                        noteId = entry.noteId,
                        date = entry.date,
                        description = entry.description,
                        entryType = entry.entryType,
                        amount = entry.amount,
                        subCategory = entry.subCategory
                    )
                },
                archives = archives.map { archive: ArchiveEntity ->
                    ArchiveExport(
                        id = archive.id,
                        type = archive.type,
                        dataJson = archive.dataJson,
                        deletedAt = archive.deletedAt
                    )
                },
                settings = settings.associate { it.key to it.value }
            )
            
            // Serialize to JSON
            val jsonString = Json.encodeToString(exportData)
            
            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "kuripot_backup_$timestamp.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(jsonString.toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importData(context: Context, filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found"))
            }
            
            // Read JSON file
            val jsonString = FileInputStream(file).use { fis ->
                fis.readBytes().toString(Charsets.UTF_8)
            }
            
            // Deserialize JSON
            val exportData = Json.decodeFromString<ExportData>(jsonString)
            
            // Import data (prevent duplicates by checking IDs)
            exportData.categories.forEach { categoryExport: CategoryExport ->
                val existingCategory = categoryRepository.getCategoryById(categoryExport.id)
                if (existingCategory == null) {
                    categoryRepository.insertCategory(
                        CategoryEntity(
                            id = categoryExport.id,
                            title = categoryExport.title,
                            isDefault = categoryExport.isDefault
                        )
                    )
                }
            }
            
            exportData.notes.forEach { noteExport: NoteExport ->
                val existingNote = noteRepository.getNoteById(noteExport.id)
                if (existingNote == null) {
                    noteRepository.insertNote(
                        NoteEntity(
                            id = noteExport.id,
                            title = noteExport.title,
                            content = noteExport.content,
                            categoryId = noteExport.categoryId,
                            createdAt = noteExport.createdAt,
                            voicePath = noteExport.voicePath,
                            isBudget = noteExport.isBudget
                        )
                    )
                }
            }
            
            exportData.budget_entries.forEach { budgetExport: BudgetEntryExport ->
                val existingEntry = budgetRepository.getBudgetEntryById(budgetExport.id)
                if (existingEntry == null) {
                    budgetRepository.insertBudgetEntry(
                        BudgetEntryEntity(
                            id = budgetExport.id,
                            noteId = budgetExport.noteId,
                            date = budgetExport.date,
                            description = budgetExport.description,
                            entryType = budgetExport.entryType,
                            amount = budgetExport.amount,
                            subCategory = budgetExport.subCategory
                        )
                    )
                }
            }
            
            // Import settings (merge strategy)
            exportData.settings.forEach { (key: String, value: String) ->
                settingRepository.setSetting(key, value)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 