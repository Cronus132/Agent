package com.ledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ledger.app.data.Transaction
import com.ledger.app.data.TransactionType
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.expenseColor
import com.ledger.app.ui.theme.incomeColor
import com.ledger.app.util.formatCents
import kotlin.math.abs

private enum class TypeFilter(val label: String) { ALL("All"), INCOME("Income"), EXPENSE("Expense") }

@Composable
fun HistoryScreen(state: FinanceUiState, onDelete: (Transaction) -> Unit) {
    var pending by remember { mutableStateOf<Transaction?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TypeFilter.ALL) }

    val filteredDays = state.days.mapNotNull { day ->
        val items = day.items.filter { t ->
            val matchesType = when (filter) {
                TypeFilter.ALL -> true
                TypeFilter.INCOME -> t.type == TransactionType.INCOME
                TypeFilter.EXPENSE -> t.type == TransactionType.EXPENSE
            }
            val matchesQuery = query.isBlank() ||
                t.title.contains(query, ignoreCase = true) ||
                t.category.contains(query, ignoreCase = true)
            matchesType && matchesQuery
        }
        if (items.isEmpty()) null else day.copy(
            items = items,
            netCents = items.sumOf {
                if (it.type == TransactionType.INCOME) it.amountCents else -it.amountCents
            }
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search item or category") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypeFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(option.label) }
                )
            }
        }

        when {
            state.days.isEmpty() ->
                EmptyHint("No history yet.\nAdd income or expenses to see them here.")

            filteredDays.isEmpty() ->
                EmptyHint("Nothing matches your search.")

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredDays.forEach { day ->
                    item(key = "header-${day.label}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(day.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = (if (day.netCents >= 0) "+" else "-") +
                                    formatCents(abs(day.netCents), state.currency),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (day.netCents >= 0) incomeColor() else expenseColor()
                            )
                        }
                    }
                    items(day.items, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            currency = state.currency,
                            onDelete = { pending = transaction }
                        )
                    }
                }
            }
        }
    }

    pending?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Delete transaction?") },
            text = { Text("\"${transaction.title}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(transaction)
                    pending = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Cancel") }
            }
        )
    }
}
