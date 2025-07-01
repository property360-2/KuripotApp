package com.malikhain.kuripot_app.data.repository

import com.malikhain.kuripot_app.data.dao.CategoryDao
import com.malikhain.kuripot_app.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: Int): CategoryEntity? = categoryDao.getCategoryById(id)
    
    suspend fun getDefaultCategory(): CategoryEntity? = categoryDao.getDefaultCategory()
    
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    
    suspend fun deleteCategory(category: CategoryEntity) {
        // Check if category has notes before deleting
        val noteCount = categoryDao.getNoteCountForCategory(category.id)
        if (noteCount == 0 && !category.isDefault) {
            categoryDao.deleteCategory(category)
        }
    }
    
    suspend fun createDefaultCategoryIfNotExists() {
        val defaultCategory = getDefaultCategory()
        if (defaultCategory == null) {
            insertCategory(CategoryEntity(title = "Budget", isDefault = true))
        }
    }
} 