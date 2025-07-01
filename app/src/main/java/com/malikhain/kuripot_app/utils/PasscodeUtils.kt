package com.malikhain.kuripot_app.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object PasscodeUtils {
    
    fun hashPasscode(passcode: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(passcode.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            // Fallback to simple hash if SHA-256 is not available
            passcode.hashCode().toString()
        }
    }
    
    fun validatePasscode(inputPasscode: String, storedHash: String): Boolean {
        val inputHash = hashPasscode(inputPasscode)
        return inputHash == storedHash
    }
    
    fun isValidPasscodeFormat(passcode: String): Boolean {
        return passcode.length == 4 && passcode.all { it.isDigit() }
    }
} 