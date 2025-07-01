package com.malikhain.kuripot_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikhain.kuripot_app.data.entities.BudgetEntryEntity
import com.malikhain.kuripot_app.data.entities.NoteEntity
import com.malikhain.kuripot_app.data.repository.BudgetRepository
import com.malikhain.kuripot_app.data.repository.NoteRepository
import com.malikhain.kuripot_app.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonthYear())
    val selectedMonth: StateFlow<String> = _selectedMonth
    
    private val _selectedEntryType = MutableStateFlow("income")
    val selectedEntryType: StateFlow<String> = _selectedEntryType
    
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
    
    init {
        loadMonthlySummary()
    }
    
    fun selectMonth(month: String) {
        _selectedMonth.value = month
        loadMonthlySummary()
    }
    
    fun selectEntryType(entryType: String) {
        _selectedEntryType.value = entryType
    }
    
    fun addBudgetEntry(
        noteId: Int,
        description: String,
        amount: Double,
        entryType: String,
        subCategory: String? = null
    ) {
        viewModelScope.launch {
            val budgetEntry = BudgetEntryEntity(
                noteId = noteId,
                date = DateUtils.getCurrentDate(),
                description = description,
                entryType = entryType,
                amount = amount,
                subCategory = subCategory
            )
            budgetRepository.insertBudgetEntry(budgetEntry)
            loadMonthlySummary()
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
            budgetRepository.deleteBudgetEntry(budgetEntry)
            loadMonthlySummary()
        }
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
} 