package com.malikhain.kuripot_app.data.dao

import androidx.room.*
import com.malikhain.kuripot_app.data.entities.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSetting(key: String): SettingEntity?
    
    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun getSettingValue(key: String): String?
    
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)
    
    @Update
    suspend fun updateSetting(setting: SettingEntity)
    
    @Delete
    suspend fun deleteSetting(setting: SettingEntity)
    
    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun deleteSettingByKey(key: String)
} 