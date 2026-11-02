
package com.ledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.expenseColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: FinanceUiState,
    onSetCurrency: (String) -> Unit,
    onClearAll: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    // Display label -> Stored currency symbol
    val currencies = listOf(
        "$" to "$",
        "€" to "€",
        "£" to "£",
        "¥" to "¥",
        "₹" to "₹",
        "₩" to "₩",
        "₽" to "₽",
        "R$" to "R$",
        "A$" to "A$",
        "C$" to "C$",
        "PKR (Rs.)" to "Rs."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Currency", style = MaterialTheme.typography.titleMedium)

        Card(Modifier.fillMaxWidth()) {
            FlowRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencies.forEach { (label, symbol) ->
                    FilterChip(
                        selected = state.currency == symbol,
                        onClick = { onSetCurrency(symbol) },
                        label = { Text(label) }
                    )
                }
            }
        }

        Text("Data", style = MaterialTheme.typography.titleMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Everything you enter is stored only on this device. The app has no internet access, so nothing is ever uploaded or shared.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = expenseColor()
                    )
                ) {
                    Icon(
                        Icons.Filled.DeleteSweep,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Clear all data")
                }
            }
        }

        Text(
            "Ledger · Offline personal finance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data?") },
            text = {
                Text(
                    "This permanently deletes every transaction. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearConfirm = false
                }) {
                    Text("Clear everything")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
