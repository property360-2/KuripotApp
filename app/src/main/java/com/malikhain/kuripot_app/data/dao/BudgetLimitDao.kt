package com.malikhain.kuripot_app.data.dao

import androidx.room.*
import com.malikhain.kuripot_app.data.entities.BudgetLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetLimitDao {
    @Query("SELECT * FROM budget_limits WHERE month = :month AND isActive = 1")
    fun getBudgetLimitsByMonth(month: String): Flow<List<BudgetLimitEntity>>
    
    @Query("SELECT * FROM budget_limits WHERE categoryId = :categoryId AND month = :month")
    suspend fun getBudgetLimitByCategoryAndMonth(categoryId: Int, month: String): BudgetLimitEntity?
    
    @Query("SELECT * FROM budget_limits WHERE isActive = 1")
    fun getAllActiveBudgetLimits(): Flow<List<BudgetLimitEntity>>
    
    @Insert
    suspend fun insertBudgetLimit(budgetLimit: BudgetLimitEntity): Long
    
    @Update
    suspend fun updateBudgetLimit(budgetLimit: BudgetLimitEntity)
    
    @Delete
    suspend fun deleteBudgetLimit(budgetLimit: BudgetLimitEntity)
    
    @Query("UPDATE budget_limits SET spent = :spent, updatedAt = :updatedAt WHERE id = :limitId")
    suspend fun updateSpentAmount(limitId: Int, spent: Double, updatedAt: String)
    
    @Query("SELECT * FROM budget_limits WHERE spent > `limit` AND isActive = 1")
    fun getOverBudgetLimits(): Flow<List<BudgetLimitEntity>>
} 