package com.ledger.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledger.app.data.CurrencyPreference
import com.ledger.app.data.LedgerRepository
import com.ledger.app.data.SavingsGoal
import com.ledger.app.data.Transaction
import com.ledger.app.data.TransactionType
import com.ledger.app.util.dayLabel
import com.ledger.app.util.startOfDay
import com.ledger.app.util.startOfMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One day's worth of transactions, plus that day's net total. */
data class DayGroup(
    val label: String,
    val netCents: Long,
    val items: List<Transaction>
)

/** Spending within one category for the current month. */
data class CategorySpend(
    val category: String,
    val cents: Long
)

/** Everything the screens need, recomputed whenever data, currency or budget changes. */
data class FinanceUiState(
    val balanceCents: Long = 0,
    val todayIncomeCents: Long = 0,
    val todayExpenseCents: Long = 0,
    val totalIncomeCents: Long = 0,
    val totalExpenseCents: Long = 0,
    val monthIncomeCents: Long = 0,
    val monthExpenseCents: Long = 0,
    val monthlyBudgetCents: Long = 0,
    val categorySpend: List<CategorySpend> = emptyList(),
    val days: List<DayGroup> = emptyList(),
    val recent: List<Transaction> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val totalSavedCents: Long = 0,
    val currency: String = CurrencyPreference.DEFAULT
) {
    /** Net saved this month (income minus expenses). Can be negative. */
    val monthNetCents: Long get() = monthIncomeCents - monthExpenseCents

    /** Share of this month's income that wasn't spent, 0..1. */
    val savingsRate: Float
        get() = if (monthIncomeCents <= 0) 0f
        else (monthNetCents.coerceAtLeast(0).toFloat() / monthIncomeCents.toFloat())
}

class FinanceViewModel(
    private val repository: LedgerRepository,
    private val currencyPref: CurrencyPreference
) : ViewModel() {

    private val currencyFlow = MutableStateFlow(currencyPref.symbol)
    private val budgetFlow = MutableStateFlow(currencyPref.monthlyBudgetCents)

    /** Reactive state: DB changes, currency and budget all flow straight to the UI. */
    val uiState: StateFlow<FinanceUiState> =
        combine(
            repository.transactions,
            repository.goals,
            currencyFlow,
            budgetFlow
        ) { list, goals, currency, budget ->
            buildState(list, goals, currency, budget)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FinanceUiState()
        )

    private fun buildState(
        list: List<Transaction>,
        goals: List<SavingsGoal>,
        currency: String,
        budgetCents: Long
    ): FinanceUiState {
        var income = 0L
        var expense = 0L
        var todayIncome = 0L
        var todayExpense = 0L
        var monthIncome = 0L
        var monthExpense = 0L
        val now = System.currentTimeMillis()
        val startOfToday = startOfDay(now)
        val startOfThisMonth = startOfMonth(now)

        for (t in list) {
            val isIncome = t.type == TransactionType.INCOME
            if (isIncome) income += t.amountCents else expense += t.amountCents
            if (t.timestamp >= startOfToday) {
                if (isIncome) todayIncome += t.amountCents else todayExpense += t.amountCents
            }
            if (t.timestamp >= startOfThisMonth) {
                if (isIncome) monthIncome += t.amountCents else monthExpense += t.amountCents
            }
        }

        val categorySpend = list
            .filter { it.type == TransactionType.EXPENSE && it.timestamp >= startOfThisMonth }
            .groupBy { it.category }
            .map { (category, items) -> CategorySpend(category, items.sumOf { it.amountCents }) }
            .sortedByDescending { it.cents }

        val days = list
            .groupBy { startOfDay(it.timestamp) }
            .toSortedMap(compareByDescending { it })
            .map { (dayStart, items) ->
                val net = items.sumOf {
                    if (it.type == TransactionType.INCOME) it.amountCents else -it.amountCents
                }
                DayGroup(label = dayLabel(dayStart), netCents = net, items = items)
            }

        return FinanceUiState(
            balanceCents = income - expense,
            todayIncomeCents = todayIncome,
            todayExpenseCents = todayExpense,
            totalIncomeCents = income,
            totalExpenseCents = expense,
            monthIncomeCents = monthIncome,
            monthExpenseCents = monthExpense,
            monthlyBudgetCents = budgetCents,
            categorySpend = categorySpend,
            days = days,
            recent = list.take(5),
            goals = goals,
            totalSavedCents = goals.sumOf { it.savedCents },
            currency = currency
        )
    }

    fun addTransaction(
        title: String,
        amountCents: Long,
        type: TransactionType,
        category: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            repository.add(
                Transaction(
                    title = title.trim().ifBlank { category },
                    amountCents = amountCents,
                    type = type,
                    category = category,
                    timestamp = timestamp
                )
            )
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { repository.delete(transaction) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }

    fun setCurrency(symbol: String) {
        currencyPref.symbol = symbol
        currencyFlow.value = symbol
    }

    fun setMonthlyBudget(cents: Long) {
        currencyPref.monthlyBudgetCents = cents
        budgetFlow.value = cents
    }

    // Savings goals ---------------------------------------------------------

    fun addGoal(name: String, targetCents: Long) {
        viewModelScope.launch {
            repository.addGoal(SavingsGoal(name = name.trim(), targetCents = targetCents))
        }
    }

    /** Add (positive) or withdraw (negative) from a goal; never drops below zero. */
    fun contributeToGoal(goal: SavingsGoal, deltaCents: Long) {
        viewModelScope.launch {
            val updated = (goal.savedCents + deltaCents).coerceAtLeast(0L)
            repository.updateGoal(goal.copy(savedCents = updated))
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    companion object {
        fun factory(
            repository: LedgerRepository,
            currencyPref: CurrencyPreference
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FinanceViewModel(repository, currencyPref) as T
        }
    }
}
