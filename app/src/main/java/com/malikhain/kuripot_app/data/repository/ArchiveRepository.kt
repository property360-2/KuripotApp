package com.malikhain.kuripot_app.data.repository

import com.malikhain.kuripot_app.data.dao.ArchiveDao
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import kotlinx.coroutines.flow.Flow

class ArchiveRepository(private val archiveDao: ArchiveDao) {
    
    fun getAllArchives(): Flow<List<ArchiveEntity>> = archiveDao.getAllArchives()
    
    fun getArchivesByType(type: String): Flow<List<ArchiveEntity>> = 
        archiveDao.getArchivesByType(type)
    
    suspend fun insertArchive(archive: ArchiveEntity): Long = 
        archiveDao.insertArchive(archive)
    
    suspend fun deleteArchive(archive: ArchiveEntity) = 
        archiveDao.deleteArchive(archive)
    
    suspend fun deleteArchiveById(id: Int) = archiveDao.deleteArchiveById(id)
    
    suspend fun getArchiveById(id: Int): ArchiveEntity? = archiveDao.getArchiveById(id)
} 