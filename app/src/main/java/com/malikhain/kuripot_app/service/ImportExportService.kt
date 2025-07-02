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
    
    private var _exportProgress = 0f
    val exportProgress: Float get() = _exportProgress
    
    private var _importProgress = 0f
    val importProgress: Float get() = _importProgress
    
    suspend fun exportData(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            _exportProgress = 0f
            
            // Collect all data
            val notes = noteRepository.getAllNotes().first()
            _exportProgress = 0.2f
            
            val categories = categoryRepository.getAllCategories().first()
            _exportProgress = 0.4f
            
            val budgetEntries = budgetRepository.getBudgetEntriesByMonth("").first()
            _exportProgress = 0.6f
            
            val archives = archiveRepository.getAllArchives().first()
            _exportProgress = 0.8f
            
            val settings = settingRepository.getAllSettings().first()
            _exportProgress = 0.9f
            
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
            
            _exportProgress = 1f
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            _exportProgress = 0f
            Result.failure(Exception("Export failed: ${e.message}"))
        }
    }
    
    suspend fun importData(context: Context, filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _importProgress = 0f
            
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found"))
            }
            
            // Check file size
            if (file.length() > 50 * 1024 * 1024) { // 50MB limit
                return@withContext Result.failure(Exception("File too large (max 50MB)"))
            }
            
            // Read JSON file
            val jsonString = FileInputStream(file).use { fis ->
                fis.readBytes().toString(Charsets.UTF_8)
            }
            _importProgress = 0.2f
            
            // Deserialize JSON
            val exportData = Json.decodeFromString<ExportData>(jsonString)
            _importProgress = 0.4f
            
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
            _importProgress = 0.8f
            
            // Import archives
            exportData.archives.forEach { archiveExport: ArchiveExport ->
                val existingArchive = archiveRepository.getArchiveById(archiveExport.id)
                if (existingArchive == null) {
                    archiveRepository.insertArchive(
                        ArchiveEntity(
                            id = archiveExport.id,
                            type = archiveExport.type,
                            dataJson = archiveExport.dataJson,
                            deletedAt = archiveExport.deletedAt
                        )
                    )
                }
            }
            _importProgress = 1f
            
            Result.success(Unit)
        } catch (e: Exception) {
            _importProgress = 0f
            Result.failure(Exception("Import failed: ${e.message}"))
        }
    }
    
    suspend fun createBackup(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            _exportProgress = 0f
            
            // Create a complete backup including all data and settings
            val backupData = ExportData(
                notes = noteRepository.getAllNotes().first().map { note ->
                    NoteExport(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        categoryId = note.categoryId,
                        createdAt = note.createdAt,
                        voicePath = note.voicePath,
                        isBudget = note.isBudget,
                        isPinned = note.isPinned
                    )
                },
                categories = categoryRepository.getAllCategories().first().map { category ->
                    CategoryExport(
                        id = category.id,
                        title = category.title,
                        isDefault = category.isDefault,
                        color = category.color,
                        icon = category.icon,
                        sortOrder = category.sortOrder
                    )
                },
                budget_entries = budgetRepository.getBudgetEntriesByMonth("").first().map { entry ->
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
                archives = archiveRepository.getAllArchives().first().map { archive ->
                    ArchiveExport(
                        id = archive.id,
                        type = archive.type,
                        dataJson = archive.dataJson,
                        deletedAt = archive.deletedAt
                    )
                },
                settings = settingRepository.getAllSettings().first().associate { it.key to it.value }
            )
            
            // Serialize to JSON
            val jsonString = Json.encodeToString(backupData)
            
            // Save to backup file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "kuripot_backup_$timestamp.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(jsonString.toByteArray())
            }
            
            _exportProgress = 1f
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            _exportProgress = 0f
            Result.failure(Exception("Backup failed: ${e.message}"))
        }
    }
    
    suspend fun restoreFromBackup(context: Context, filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _importProgress = 0f
            
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Backup file not found"))
            }
            
            // Check file size
            if (file.length() > 100 * 1024 * 1024) { // 100MB limit for backups
                return@withContext Result.failure(Exception("Backup file too large (max 100MB)"))
            }
            
            // Read backup file
            val jsonString = FileInputStream(file).use { fis ->
                fis.readBytes().toString(Charsets.UTF_8)
            }
            _importProgress = 0.2f
            
            // Deserialize backup data
            val backupData = Json.decodeFromString<ExportData>(jsonString)
            _importProgress = 0.4f
            
            // Clear existing data (optional - could be made configurable)
            // For now, we'll merge data to avoid data loss
            
            // Restore categories
            backupData.categories.forEach { categoryExport ->
                val existingCategory = categoryRepository.getCategoryById(categoryExport.id)
                if (existingCategory == null) {
                    categoryRepository.insertCategory(
                        CategoryEntity(
                            id = categoryExport.id,
                            title = categoryExport.title,
                            isDefault = categoryExport.isDefault,
                            color = categoryExport.color ?: "#FF6200EE",
                            icon = categoryExport.icon ?: "label",
                            sortOrder = categoryExport.sortOrder ?: 0
                        )
                    )
                }
            }
            _importProgress = 0.6f
            
            // Restore notes
            backupData.notes.forEach { noteExport ->
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
                            isBudget = noteExport.isBudget,
                            isPinned = noteExport.isPinned ?: false
                        )
                    )
                }
            }
            _importProgress = 0.8f
            
            // Restore budget entries
            backupData.budget_entries.forEach { budgetExport ->
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
            
            // Restore settings
            backupData.settings.forEach { (key, value) ->
                settingRepository.setSetting(key, value)
            }
            
            _importProgress = 1f
            Result.success(Unit)
        } catch (e: Exception) {
            _importProgress = 0f
            Result.failure(Exception("Restore failed: ${e.message}"))
        }
    }
} 