package com.malikhain.kuripot_app.data.repository

import com.malikhain.kuripot_app.data.dao.BudgetEntryDao
import com.malikhain.kuripot_app.data.dao.ArchiveDao
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import com.malikhain.kuripot_app.model.BudgetEntryExport
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.malikhain.kuripot_app.utils.DateUtils

class BudgetRepository(
    private val budgetEntryDao: BudgetEntryDao,
    private val archiveDao: ArchiveDao
) {
    
    fun getBudgetEntriesByNoteId(noteId: Int): Flow<List<BudgetEntryEntity>> = 
        budgetEntryDao.getBudgetEntriesByNoteId(noteId)
    
    fun getBudgetEntriesByType(entryType: String): Flow<List<BudgetEntryEntity>> = 
        budgetEntryDao.getBudgetEntriesByType(entryType)
    
    fun getBudgetEntriesByMonth(monthYear: String): Flow<List<BudgetEntryEntity>> = 
        budgetEntryDao.getBudgetEntriesByMonth(monthYear)
    
    suspend fun getTotalIncomeForMonth(monthYear: String): Double = 
        budgetEntryDao.getTotalIncomeForMonth(monthYear) ?: 0.0
    
    suspend fun getTotalExpenseForMonth(monthYear: String): Double = 
        budgetEntryDao.getTotalExpenseForMonth(monthYear) ?: 0.0
    
    suspend fun getBalanceForMonth(monthYear: String): Double {
        val income = getTotalIncomeForMonth(monthYear)
        val expense = getTotalExpenseForMonth(monthYear)
        return income - expense
    }
    
    suspend fun insertBudgetEntry(budgetEntry: BudgetEntryEntity): Long = 
        budgetEntryDao.insertBudgetEntry(budgetEntry)
    
    suspend fun updateBudgetEntry(budgetEntry: BudgetEntryEntity) = 
        budgetEntryDao.updateBudgetEntry(budgetEntry)
    
    suspend fun deleteBudgetEntry(budgetEntry: BudgetEntryEntity) {
        // Archive the budget entry before deleting
        val budgetEntryExport = BudgetEntryExport(
            id = budgetEntry.id,
            noteId = budgetEntry.noteId,
            date = budgetEntry.date,
            description = budgetEntry.description,
            entryType = budgetEntry.entryType,
            amount = budgetEntry.amount,
            subCategory = budgetEntry.subCategory
        )
        
        val archive = ArchiveEntity(
            type = "budget",
            dataJson = Json.encodeToString(budgetEntryExport),
            deletedAt = DateUtils.getCurrentDate()
        )
        
        archiveDao.insertArchive(archive)
        budgetEntryDao.deleteBudgetEntry(budgetEntry)
    }
    
    suspend fun getBudgetEntryById(id: Int): BudgetEntryEntity? = 
        budgetEntryDao.getBudgetEntryById(id)
} 