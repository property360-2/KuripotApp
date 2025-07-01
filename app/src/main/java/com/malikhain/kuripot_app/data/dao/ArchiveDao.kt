package com.malikhain.kuripot_app.data.dao

import androidx.room.*
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM archives ORDER BY deletedAt DESC")
    fun getAllArchives(): Flow<List<ArchiveEntity>>
    
    @Query("SELECT * FROM archives WHERE type = :type ORDER BY deletedAt DESC")
    fun getArchivesByType(type: String): Flow<List<ArchiveEntity>>
    
    @Insert
    suspend fun insertArchive(archive: ArchiveEntity): Long
    
    @Delete
    suspend fun deleteArchive(archive: ArchiveEntity)
    
    @Query("DELETE FROM archives WHERE id = :id")
    suspend fun deleteArchiveById(id: Int)
    
    @Query("SELECT * FROM archives WHERE id = :id")
    suspend fun getArchiveById(id: Int): ArchiveEntity?
} 