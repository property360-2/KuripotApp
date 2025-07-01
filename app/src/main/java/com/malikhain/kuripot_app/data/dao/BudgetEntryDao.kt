package com.malikhain.kuripot_app.data.dao

import androidx.room.*
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetEntryDao {
    @Query("SELECT * FROM budget_entries WHERE noteId = :noteId ORDER BY date DESC")
    fun getBudgetEntriesByNoteId(noteId: Int): Flow<List<BudgetEntryEntity>>
    
    @Query("SELECT * FROM budget_entries WHERE entryType = :entryType ORDER BY date DESC")
    fun getBudgetEntriesByType(entryType: String): Flow<List<BudgetEntryEntity>>
    
    @Query("SELECT * FROM budget_entries WHERE date LIKE :monthYear || '%' ORDER BY date DESC")
    fun getBudgetEntriesByMonth(monthYear: String): Flow<List<BudgetEntryEntity>>
    
    @Query("SELECT SUM(amount) FROM budget_entries WHERE entryType = 'income' AND date LIKE :monthYear || '%'")
    suspend fun getTotalIncomeForMonth(monthYear: String): Double?
    
    @Query("SELECT SUM(amount) FROM budget_entries WHERE entryType = 'expense' AND date LIKE :monthYear || '%'")
    suspend fun getTotalExpenseForMonth(monthYear: String): Double?
    
    @Insert
    suspend fun insertBudgetEntry(budgetEntry: BudgetEntryEntity): Long
    
    @Update
    suspend fun updateBudgetEntry(budgetEntry: BudgetEntryEntity)
    
    @Delete
    suspend fun deleteBudgetEntry(budgetEntry: BudgetEntryEntity)
    
    @Query("SELECT * FROM budget_entries WHERE id = :id")
    suspend fun getBudgetEntryById(id: Int): BudgetEntryEntity?
} 