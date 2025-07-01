package com.malikhain.kuripot_app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    fun getCurrentMonthYear(): String {
        return monthYearFormat.format(Date())
    }
    
    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun formatMonthYearForDisplay(monthYear: String): String {
        return try {
            val date = monthYearFormat.parse(monthYear)
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date ?: Date())
        } catch (e: Exception) {
            monthYear
        }
    }
} 