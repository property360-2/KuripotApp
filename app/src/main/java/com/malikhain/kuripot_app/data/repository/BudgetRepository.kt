package com.malikhain.kuripot_app.data.repository

import com.malikhain.kuripot_app.data.dao.BudgetEntryDao
import com.malikhain.kuripot_app.data.dao.ArchiveDao
import com.malikhain.kuripot_app.data.dao.BudgetLimitDao
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import com.malikhain.kuripot_app.data.entities.BudgetLimitEntity
import com.malikhain.kuripot_app.model.BudgetEntryExport
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.malikhain.kuripot_app.utils.DateUtils

class BudgetRepository(
    private val budgetEntryDao: BudgetEntryDao,
    private val archiveDao: ArchiveDao,
    private val budgetLimitDao: BudgetLimitDao
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
    
    suspend fun insertBudgetLimit(limit: BudgetLimitEntity) = budgetLimitDao.insertBudgetLimit(limit)
    suspend fun updateBudgetLimit(limit: BudgetLimitEntity) = budgetLimitDao.updateBudgetLimit(limit)
    suspend fun deleteBudgetLimit(limit: BudgetLimitEntity) = budgetLimitDao.deleteBudgetLimit(limit)
    fun getBudgetLimitsByMonth(month: String) = budgetLimitDao.getBudgetLimitsByMonth(month)
    suspend fun getBudgetLimitByCategoryAndMonth(categoryId: Int, month: String) = budgetLimitDao.getBudgetLimitByCategoryAndMonth(categoryId, month)
    fun getAllActiveBudgetLimits() = budgetLimitDao.getAllActiveBudgetLimits()
    suspend fun updateSpentAmount(limitId: Int, spent: Double, updatedAt: String) = budgetLimitDao.updateSpentAmount(limitId, spent, updatedAt)
    fun getOverBudgetLimits() = budgetLimitDao.getOverBudgetLimits()
    
    // Recurring logic
    suspend fun getRecurringEntries() = budgetEntryDao.getRecurringEntries()
    suspend fun updateNextRecurringDate(entryId: Int, nextDate: String) = budgetEntryDao.updateNextRecurringDate(entryId, nextDate)
} 