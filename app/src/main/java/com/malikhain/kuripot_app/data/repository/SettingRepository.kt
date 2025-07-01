package com.malikhain.kuripot_app.data.repository

import com.malikhain.kuripot_app.data.dao.SettingDao
import com.malikhain.kuripot_app.data.entities.SettingEntity
import com.malikhain.kuripot_app.utils.PasscodeUtils
import com.malikhain.kuripot_app.utils.ThemeUtils
import kotlinx.coroutines.flow.Flow

class SettingRepository(private val settingDao: SettingDao) {
    
    fun getAllSettings(): Flow<List<SettingEntity>> = settingDao.getAllSettings()
    
    suspend fun getSetting(key: String): SettingEntity? = settingDao.getSetting(key)
    
    suspend fun getSettingValue(key: String): String? = settingDao.getSettingValue(key)
    
    suspend fun setSetting(key: String, value: String) {
        settingDao.insertSetting(SettingEntity(key, value))
    }
    
    suspend fun updateSetting(setting: SettingEntity) = settingDao.updateSetting(setting)
    
    suspend fun deleteSetting(key: String) = settingDao.deleteSettingByKey(key)
    
    // Theme management
    suspend fun getTheme(): String = getSettingValue(ThemeUtils.THEME_KEY) ?: ThemeUtils.THEME_SYSTEM
    
    suspend fun setTheme(theme: String) = setSetting(ThemeUtils.THEME_KEY, theme)
    
    // Passcode management
    suspend fun getPasscodeHash(): String? = getSettingValue("passcode")
    
    suspend fun setPasscode(passcode: String) {
        if (PasscodeUtils.isValidPasscodeFormat(passcode)) {
            val hashedPasscode = PasscodeUtils.hashPasscode(passcode)
            setSetting("passcode", hashedPasscode)
        }
    }
    
    suspend fun isPasscodeSet(): Boolean = getPasscodeHash() != null
} 