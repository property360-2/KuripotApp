package com.malikhain.kuripot_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikhain.kuripot_app.data.dao.SettingDao
import com.malikhain.kuripot_app.utils.PasscodeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasscodeViewModel(private val settingDao: SettingDao) : ViewModel() {
    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked

    private val _isPasscodeSet = MutableStateFlow(false)
    val isPasscodeSet: StateFlow<Boolean> = _isPasscodeSet

    private val _showForgotPasscodeDialog = MutableStateFlow(false)
    val showForgotPasscodeDialog: StateFlow<Boolean> = _showForgotPasscodeDialog

    init {
        viewModelScope.launch {
            _isPasscodeSet.value = settingDao.getSettingValue("passcode") != null
        }
    }

    fun onPinChange(newPin: String) {
        if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
            _pin.value = newPin
            _error.value = null
        }
    }

    fun submitPin() {
        viewModelScope.launch {
            val storedHash = settingDao.getSettingValue("passcode")
            if (storedHash == null) {
                _error.value = "No passcode set. Please set up your passcode."
                return@launch
            }
            if (PasscodeUtils.validatePasscode(_pin.value, storedHash)) {
                _unlocked.value = true
            } else {
                _error.value = "Incorrect passcode."
                _pin.value = ""
                _showForgotPasscodeDialog.value = true
            }
        }
    }

    fun setPasscode(passcode: String, confirm: String) {
        viewModelScope.launch {
            if (passcode.length != 4 || !passcode.all { it.isDigit() }) {
                _error.value = "Passcode must be 4 digits."
                return@launch
            }
            if (passcode != confirm) {
                _error.value = "Passcodes do not match."
                return@launch
            }
            val hash = PasscodeUtils.hashPasscode(passcode)
            settingDao.insertSetting(com.malikhain.kuripot_app.data.entities.SettingEntity(key = "passcode", value = hash))
            _isPasscodeSet.value = true
            _unlocked.value = true
        }
    }

    fun showForgotPasscodeDialog() {
        _showForgotPasscodeDialog.value = true
    }

    fun hideForgotPasscodeDialog() {
        _showForgotPasscodeDialog.value = false
    }

    fun resetPasscode() {
        viewModelScope.launch {
            settingDao.deleteSettingByKey("passcode")
            _isPasscodeSet.value = false
            _showForgotPasscodeDialog.value = false
            _error.value = null
        }
    }
} 