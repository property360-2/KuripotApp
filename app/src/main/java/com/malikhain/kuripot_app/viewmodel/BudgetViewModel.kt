package com.malikhain.kuripot_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.repository.BudgetRepository
import com.malikhain.kuripot_app.data.repository.NoteRepository
import com.malikhain.kuripot_app.utils.DateUtils
import com.malikhain.kuripot_app.ui.theme.*
import androidx.compose.ui.graphics.Color
import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.malikhain.kuripot_app.data.entities.BudgetLimitEntity

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonthYear())
    val selectedMonth: StateFlow<String> = _selectedMonth
    
    private val _selectedEntryType = MutableStateFlow("income")
    val selectedEntryType: StateFlow<String> = _selectedEntryType
    
    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory
    
    private val _availableSubcategories = MutableStateFlow<List<String>>(emptyList())
    val availableSubcategories: StateFlow<List<String>> = _availableSubcategories
    
    private val _lastDeletedEntry = MutableStateFlow<BudgetEntryEntity?>(null)
    val lastDeletedEntry: StateFlow<BudgetEntryEntity?> = _lastDeletedEntry
    
    val budgetEntries: StateFlow<List<BudgetEntryEntity>> = _selectedMonth
        .combine(_selectedEntryType) { month, entryType ->
            Pair(month, entryType)
        }
        .flatMapLatest { (month, entryType) ->
            if (entryType == "all") {
                budgetRepository.getBudgetEntriesByMonth(month)
            } else {
                budgetRepository.getBudgetEntriesByType(entryType)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val budgetNotes: StateFlow<List<NoteEntity>> = noteRepository.getBudgetNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome
    
    private val _monthlyExpense = MutableStateFlow(0.0)
    val monthlyExpense: StateFlow<Double> = _monthlyExpense
    
    private val _monthlyBalance = MutableStateFlow(0.0)
    val monthlyBalance: StateFlow<Double> = _monthlyBalance
    
    private val _showMonthPicker = MutableStateFlow(false)
    val showMonthPicker: StateFlow<Boolean> = _showMonthPicker
    
    private val _expenseChartData = MutableStateFlow<List<PieChartData>>(emptyList())
    val expenseChartData: StateFlow<List<PieChartData>> = _expenseChartData
    
    private val _incomeExpenseTrend = MutableStateFlow<List<LineChartData>>(emptyList())
    val incomeExpenseTrend: StateFlow<List<LineChartData>> = _incomeExpenseTrend
    
    private val _budgetLimits = MutableStateFlow<List<BudgetLimitEntity>>(emptyList())
    val budgetLimits: StateFlow<List<BudgetLimitEntity>> = _budgetLimits
    private val _overBudgetAlerts = MutableStateFlow<List<BudgetLimitEntity>>(emptyList())
    val overBudgetAlerts: StateFlow<List<BudgetLimitEntity>> = _overBudgetAlerts
    
    init {
        loadMonthlySummary()
        loadChartData()
        loadBudgetLimits()
        checkRecurringEntries()
    }
    
    fun selectMonth(month: String) {
        _selectedMonth.value = month
        loadMonthlySummary()
        loadChartData()
    }
    
    fun selectEntryType(entryType: String) {
        _selectedEntryType.value = entryType
    }
    
    fun selectSubcategory(subcategory: String?) {
        _selectedSubcategory.value = subcategory
        loadFilteredEntries()
    }
    
    fun loadAvailableSubcategories() {
        viewModelScope.launch {
            val entries = budgetRepository.getBudgetEntriesByMonth(_selectedMonth.value).first()
            val subcategories = entries
                .mapNotNull { it.subCategory }
                .distinct()
                .sorted()
            _availableSubcategories.value = subcategories
        }
    }
    
    private fun loadFilteredEntries() {
        // This is handled by the StateFlow combination in budgetEntries
        // No need to manually filter as it's done automatically
    }
    
    fun addBudgetEntry(
        noteId: Int,
        description: String,
        amount: Double,
        entryType: String,
        subCategory: String? = null,
        isRecurring: Boolean = false,
        recurringFrequency: String? = null
    ) {
        viewModelScope.launch {
            val currentDate = DateUtils.getCurrentDate()
            val nextRecurringDate = if (isRecurring && recurringFrequency != null) {
                getNextRecurringDate(currentDate, recurringFrequency)
            } else null
            
            val budgetEntry = BudgetEntryEntity(
                noteId = noteId,
                date = currentDate,
                description = description,
                entryType = entryType,
                amount = amount,
                subCategory = subCategory,
                isRecurring = isRecurring,
                recurringFrequency = recurringFrequency,
                nextRecurringDate = nextRecurringDate
            )
            budgetRepository.insertBudgetEntry(budgetEntry)
            loadMonthlySummary()
            updateSpentForLimits()
        }
    }
    
    fun updateBudgetEntry(budgetEntry: BudgetEntryEntity) {
        viewModelScope.launch {
            budgetRepository.updateBudgetEntry(budgetEntry)
            loadMonthlySummary()
        }
    }
    
    fun deleteBudgetEntry(budgetEntry: BudgetEntryEntity) {
        viewModelScope.launch {
            _lastDeletedEntry.value = budgetEntry
            budgetRepository.deleteBudgetEntry(budgetEntry)
            loadMonthlySummary()
            updateSpentForLimits()
        }
    }
    
    fun undoDelete() {
        viewModelScope.launch {
            _lastDeletedEntry.value?.let { entry ->
                budgetRepository.insertBudgetEntry(entry)
                _lastDeletedEntry.value = null
                loadMonthlySummary()
                updateSpentForLimits()
            }
        }
    }
    
    fun clearLastDeletedEntry() {
        _lastDeletedEntry.value = null
    }
    
    private fun loadMonthlySummary() {
        viewModelScope.launch {
            val month = _selectedMonth.value
            val income = budgetRepository.getTotalIncomeForMonth(month)
            val expense = budgetRepository.getTotalExpenseForMonth(month)
            val balance = budgetRepository.getBalanceForMonth(month)
            
            _monthlyIncome.value = income
            _monthlyExpense.value = expense
            _monthlyBalance.value = balance
        }
    }
    
    fun getAvailableMonths(): List<String> {
        // This would typically query the database for available months
        // For now, return current month and a few previous months
        val currentMonth = DateUtils.getCurrentMonthYear()
        return listOf(currentMonth) // Simplified for now
    }
    
    fun showMonthPicker() {
        _showMonthPicker.value = true
    }
    
    fun hideMonthPicker() {
        _showMonthPicker.value = false
    }
    
    private fun loadChartData() {
        viewModelScope.launch {
            val month = _selectedMonth.value
            val entries = budgetRepository.getBudgetEntriesByMonth(month).first()
            
            // Load expense chart data by subcategory
            val expenseEntries = entries.filter { it.entryType == "expense" }
            val expenseByCategory = expenseEntries.groupBy { it.subCategory ?: "Other" }
            val chartData = expenseByCategory.map { (category, categoryEntries) ->
                val total = categoryEntries.sumOf { it.amount }.toFloat()
                val color = when (category) {
                    "Food" -> Color(0xFFE57373)
                    "Transport" -> Color(0xFF81C784)
                    "Entertainment" -> Color(0xFFFFB74D)
                    "Shopping" -> Color(0xFF64B5F6)
                    "Bills" -> Color(0xFFBA68C8)
                    else -> Color(0xFF90A4AE)
                }
                PieChartData(category, total, color)
            }
            _expenseChartData.value = chartData
            
            // Load income/expense trend (last 6 months)
            val trendData = mutableListOf<LineChartData>()
            // This would typically query historical data
            // For now, using current month data as placeholder
            trendData.add(LineChartData("Income", _monthlyIncome.value.toFloat()))
            trendData.add(LineChartData("Expense", _monthlyExpense.value.toFloat()))
            _incomeExpenseTrend.value = trendData
        }
    }
    
    fun exportToCSV(context: Context): Result<String> {
        return try {
            val month = _selectedMonth.value
            val entries = budgetRepository.getBudgetEntriesByMonth(month).first()
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "budget_export_${month}_$timestamp.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val csvContent = buildString {
                appendLine("Date,Description,Type,Subcategory,Amount")
                entries.forEach { entry ->
                    appendLine("${entry.date},${entry.description},${entry.entryType},${entry.subCategory ?: ""},${entry.amount}")
                }
            }
            
            file.writeText(csvContent)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun loadBudgetLimits() {
        viewModelScope.launch {
            budgetRepository.getBudgetLimitsByMonth(_selectedMonth.value).collect {
                _budgetLimits.value = it
            }
            budgetRepository.getOverBudgetLimits().collect {
                _overBudgetAlerts.value = it
            }
        }
    }
    
    fun setBudgetLimit(categoryId: Int, month: String, limit: Double) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val existing = budgetRepository.getBudgetLimitByCategoryAndMonth(categoryId, month)
            if (existing == null) {
                budgetRepository.insertBudgetLimit(BudgetLimitEntity(
                    categoryId = categoryId,
                    month = month,
                    limit = limit,
                    spent = 0.0,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                ))
            } else {
                budgetRepository.updateBudgetLimit(existing.copy(limit = limit, updatedAt = now))
            }
            loadBudgetLimits()
        }
    }
    
    fun updateSpentForLimits() {
        viewModelScope.launch {
            val month = _selectedMonth.value
            val limits = budgetRepository.getBudgetLimitsByMonth(month).first()
            limits.forEach { limit ->
                val spent = budgetEntries.value.filter { it.subCategory == null && it.entryType == "expense" && it.date.startsWith(month) }.sumOf { it.amount }
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                budgetRepository.updateSpentAmount(limit.id, spent, now)
            }
            loadBudgetLimits()
        }
    }
    
    fun checkRecurringEntries() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val recEntries = budgetRepository.getRecurringEntries()
            recEntries.forEach { entry ->
                if (entry.nextRecurringDate != null && entry.nextRecurringDate <= today) {
                    // Generate new entry for this recurrence
                    val newDate = getNextRecurringDate(entry.nextRecurringDate, entry.recurringFrequency ?: "monthly")
                    val newEntry = entry.copy(
                        id = 0,
                        date = entry.nextRecurringDate,
                        nextRecurringDate = newDate
                    )
                    budgetRepository.insertBudgetEntry(newEntry)
                    budgetRepository.updateNextRecurringDate(entry.id, newDate)
                }
            }
        }
    }
    
    private fun getNextRecurringDate(current: String?, freq: String?): String? {
        if (current == null || freq == null) return null
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(current) ?: return null
        when (freq) {
            "weekly" -> cal.add(Calendar.DATE, 7)
            "monthly" -> cal.add(Calendar.MONTH, 1)
            "yearly" -> cal.add(Calendar.YEAR, 1)
        }
        return sdf.format(cal.time)
    }
} 