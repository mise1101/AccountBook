package com.example.accountbook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountbook.data.AppDatabase
import com.example.accountbook.data.entity.Category
import com.example.accountbook.data.entity.Transaction
import com.example.accountbook.data.entity.TransactionWithCategory
import com.example.accountbook.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TransactionFilter { ALL, EXPENSE, INCOME }

data class TransactionFormState(
    val amount: String = "",
    val type: String = "EXPENSE",
    val categoryId: Long? = null,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AppRepository(database.categoryDao(), database.transactionDao())
    }

    // Filter
    private val _filter = MutableStateFlow(TransactionFilter.ALL)
    val filter: StateFlow<TransactionFilter> = _filter

    // Current month for stats
    private val _currentMonthOffset = MutableStateFlow(0)
    val currentMonthOffset: StateFlow<Int> = _currentMonthOffset

    // Form state
    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState

    // Categories
    val expenseCategories: StateFlow<List<Category>> = repository
        .getCategoriesByType("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> = repository
        .getCategoriesByType("INCOME")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions based on filter
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionWithCategory>> = _filter
        .flatMapLatest { f ->
            when (f) {
                TransactionFilter.ALL -> repository.allTransactions
                TransactionFilter.EXPENSE -> repository.getTransactionsByType("EXPENSE")
                TransactionFilter.INCOME -> repository.getTransactionsByType("INCOME")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly totals
    private val monthRange: Pair<Long, Long>
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, _currentMonthOffset.value)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis - 1
            return start to end
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyExpense: StateFlow<Double> = combine(
        _currentMonthOffset, _filter
    ) { _, _ -> Unit }
        .flatMapLatest {
            val (start, end) = monthRange
            repository.getTotalByTypeAndDate("EXPENSE", start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyIncome: StateFlow<Double> = combine(
        _currentMonthOffset, _filter
    ) { _, _ -> Unit }
        .flatMapLatest {
            val (start, end) = monthRange
            repository.getTotalByTypeAndDate("INCOME", start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Actions
    fun setFilter(f: TransactionFilter) { _filter.value = f }

    fun previousMonth() { _currentMonthOffset.value -= 1 }
    fun nextMonth() {
        if (_currentMonthOffset.value < 0) {
            _currentMonthOffset.value += 1
        }
    }
    fun resetMonth() { _currentMonthOffset.value = 0 }

    fun updateFormAmount(amount: String) {
        _formState.value = _formState.value.copy(amount = amount)
    }

    fun updateFormType(type: String) {
        _formState.value = _formState.value.copy(
            type = type,
            categoryId = null // reset category when switching type
        )
    }

    fun updateFormCategory(categoryId: Long) {
        _formState.value = _formState.value.copy(categoryId = categoryId)
    }

    fun updateFormNote(note: String) {
        _formState.value = _formState.value.copy(note = note)
    }

    fun updateFormDate(millis: Long) {
        _formState.value = _formState.value.copy(dateMillis = millis)
    }

    fun resetForm() {
        _formState.value = TransactionFormState()
    }

    fun isFormValid(): Boolean {
        val s = _formState.value
        return s.amount.toDoubleOrNull() != null &&
                (s.amount.toDouble()) > 0 &&
                s.categoryId != null
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val s = _formState.value
        val amount = s.amount.toDoubleOrNull() ?: return
        if (amount <= 0 || s.categoryId == null) return

        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    type = s.type,
                    categoryId = s.categoryId,
                    note = s.note,
                    date = s.dateMillis
                )
            )
            resetForm()
            onSuccess()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Auto-select first category when type changes and none selected
    fun autoSelectCategory() {
        val s = _formState.value
        if (s.categoryId == null) {
            val cats = if (s.type == "EXPENSE") expenseCategories.value else incomeCategories.value
            if (cats.isNotEmpty()) {
                _formState.value = s.copy(categoryId = cats.first().id)
            }
        }
    }

    // Category management
    fun addCustomCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.addCategory(
                Category(name = name, type = type, isPredefined = false)
            )
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
