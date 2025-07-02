package com.malikhain.kuripot_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikhain.kuripot_app.data.entities.ArchiveEntity
import com.malikhain.kuripot_app.data.repository.ArchiveRepository
import com.malikhain.kuripot_app.data.repository.SettingRepository
import com.malikhain.kuripot_app.service.ImportExportService
import com.malikhain.kuripot_app.utils.PasscodeUtils
import com.malikhain.kuripot_app.utils.ThemeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context

class SettingsViewModel(
    private val settingRepository: SettingRepository,
    private val archiveRepository: ArchiveRepository,
    private val importExportService: ImportExportService
) : ViewModel() {
    
    private val _currentTheme = MutableStateFlow(ThemeUtils.THEME_SYSTEM)
    val currentTheme: StateFlow<String> = _currentTheme
    
    private val _isPasscodeSet = MutableStateFlow(false)
    val isPasscodeSet: StateFlow<Boolean> = _isPasscodeSet
    
    private val _archives = MutableStateFlow<List<ArchiveEntity>>(emptyList())
    val archives: StateFlow<List<ArchiveEntity>> = _archives
    
    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus
    
    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus
    
    private val _showImportFilePicker = MutableStateFlow(false)
    val showImportFilePicker: StateFlow<Boolean> = _showImportFilePicker
    
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus
    
    private val _restoreStatus = MutableStateFlow<String?>(null)
    val restoreStatus: StateFlow<String?> = _restoreStatus
    
    init {
        loadSettings()
        loadArchives()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _currentTheme.value = settingRepository.getTheme()
            _isPasscodeSet.value = settingRepository.isPasscodeSet()
        }
    }
    
    private fun loadArchives() {
        viewModelScope.launch {
            archiveRepository.getAllArchives().collect { archives ->
                _archives.value = archives
            }
        }
    }
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingRepository.setTheme(theme)
            _currentTheme.value = theme
        }
    }
    
    fun setPasscode(passcode: String): Boolean {
        return if (PasscodeUtils.isValidPasscodeFormat(passcode)) {
            viewModelScope.launch {
                settingRepository.setPasscode(passcode)
                _isPasscodeSet.value = true
            }
            true
        } else {
            false
        }
    }
    
    fun changePasscode(currentPasscode: String, newPasscode: String): Boolean {
        return if (PasscodeUtils.isValidPasscodeFormat(newPasscode)) {
            viewModelScope.launch {
                val currentHash = settingRepository.getPasscodeHash()
                if (currentHash != null && PasscodeUtils.validatePasscode(currentPasscode, currentHash)) {
                    settingRepository.setPasscode(newPasscode)
                }
            }
            true
        } else {
            false
        }
    }
    
    fun exportData(context: Context) {
        viewModelScope.launch {
            _exportStatus.value = "Exporting..."
            importExportService.exportData(context).onSuccess { filePath ->
                _exportStatus.value = "Exported to: $filePath"
            }.onFailure { exception ->
                _exportStatus.value = "Export failed: ${exception.message}"
            }
        }
    }
    
    fun importData(context: Context, filePath: String) {
        viewModelScope.launch {
            _importStatus.value = "Importing..."
            importExportService.importData(context, filePath).onSuccess {
                _importStatus.value = "Import successful"
                loadSettings() // Reload settings after import
            }.onFailure { exception ->
                _importStatus.value = "Import failed: ${exception.message}"
            }
        }
    }
    
    fun restoreArchive(archive: ArchiveEntity) {
        viewModelScope.launch {
            // This would parse the JSON and restore the item
            // For now, just delete from archive
            archiveRepository.deleteArchive(archive)
        }
    }
    
    fun permanentlyDeleteArchive(archive: ArchiveEntity) {
        viewModelScope.launch {
            archiveRepository.deleteArchive(archive)
        }
    }
    
    fun clearExportStatus() {
        _exportStatus.value = null
    }
    
    fun clearImportStatus() {
        _importStatus.value = null
    }
    
    fun showImportFilePicker(context: Context) {
        _showImportFilePicker.value = true
    }
    
    fun hideImportFilePicker() {
        _showImportFilePicker.value = false
    }
    
    fun createBackup(context: Context) {
        viewModelScope.launch {
            _backupStatus.value = "Creating backup..."
            importExportService.createBackup(context).onSuccess { filePath ->
                _backupStatus.value = "Backup created: $filePath"
            }.onFailure { exception ->
                _backupStatus.value = "Backup failed: ${exception.message}"
            }
        }
    }
    
    fun restoreFromBackup(context: Context, filePath: String) {
        viewModelScope.launch {
            _restoreStatus.value = "Restoring from backup..."
            importExportService.restoreFromBackup(context, filePath).onSuccess {
                _restoreStatus.value = "Restore successful"
                loadSettings() // Reload settings after restore
                loadArchives() // Reload archives after restore
            }.onFailure { exception ->
                _restoreStatus.value = "Restore failed: ${exception.message}"
            }
        }
    }
    
    fun clearBackupStatus() {
        _backupStatus.value = null
    }
    
    fun clearRestoreStatus() {
        _restoreStatus.value = null
    }
} 