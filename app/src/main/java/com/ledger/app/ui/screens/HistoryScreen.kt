package com.ledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.expenseColor
import com.ledger.app.ui.theme.incomeColor
import com.ledger.app.util.formatCents
import kotlin.math.abs

@Composable
fun HistoryScreen(state: FinanceUiState, onDelete: (Transaction) -> Unit) {
    var pending by remember { mutableStateOf<Transaction?>(null) }

    if (state.days.isEmpty()) {
        EmptyHint("No history yet.\nAdd income or expenses to see them here.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        state.days.forEach { day ->
            item(key = "header-${day.label}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
