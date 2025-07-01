package com.malikhain.kuripot_app.data.dao

import androidx.room.*
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY title ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCategory(): CategoryEntity?
    
    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("SELECT COUNT(*) FROM notes WHERE categoryId = :categoryId")
    suspend fun getNoteCountForCategory(categoryId: Int): Int
} 