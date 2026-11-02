package com.ledger.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledger.app.data.CurrencyPreference
import com.ledger.app.data.Transaction
import com.ledger.app.data.TransactionRepository
import com.ledger.app.data.TransactionType
import com.ledger.app.util.dayLabel
import com.ledger.app.util.startOfDay
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

/** Everything the screens need, recomputed whenever data or currency changes. */
data class FinanceUiState(
    val balanceCents: Long = 0,
    val todayIncomeCents: Long = 0,
    val todayExpenseCents: Long = 0,
    val totalIncomeCents: Long = 0,
    val totalExpenseCents: Long = 0,
    val days: List<DayGroup> = emptyList(),
    val recent: List<Transaction> = emptyList(),
    val currency: String = CurrencyPreference.DEFAULT
)

class FinanceViewModel(
    private val repository: TransactionRepository,
    private val currencyPref: CurrencyPreference
) : ViewModel() {

    private val currencyFlow = MutableStateFlow(currencyPref.symbol)

    /** Reactive state: SQLite changes and currency changes both flow straight to the UI. */
    val uiState: StateFlow<FinanceUiState> =
        combine(repository.transactions, currencyFlow) { list, currency ->
            buildState(list, currency)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FinanceUiState()
        )

    private fun buildState(list: List<Transaction>, currency: String): FinanceUiState {
        var income = 0L
        var expense = 0L
        var todayIncome = 0L
        var todayExpense = 0L
        val startOfToday = startOfDay(System.currentTimeMillis())

        for (t in list) {
            if (t.type == TransactionType.INCOME) income += t.amountCents else expense += t.amountCents
            if (t.timestamp >= startOfToday) {
                if (t.type == TransactionType.INCOME) todayIncome += t.amountCents
                else todayExpense += t.amountCents
            }
        }

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
            days = days,
            recent = list.take(5),
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

    companion object {
        fun factory(
            repository: TransactionRepository,
            currencyPref: CurrencyPreference
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FinanceViewModel(repository, currencyPref) as T
        }
    }
}
