package com.ledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.expenseColor
import com.ledger.app.ui.theme.incomeColor

@Composable
fun DashboardScreen(state: FinanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BalanceCard(state)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryTile(
                label = "Earned today",
                cents = state.todayIncomeCents,
                accent = incomeColor(),
                currency = state.currency,
                modifier = Modifier.weight(1f)
            )
            SummaryTile(
                label = "Spent today",
                cents = state.todayExpenseCents,
                accent = expenseColor(),
                currency = state.currency,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Recent", style = MaterialTheme.typography.titleMedium)

        if (state.recent.isEmpty()) {
            EmptyHint("No transactions yet.\nTap + to add your first one.")
        } else {
            state.recent.forEach { transaction ->
                TransactionRow(transaction, state.currency)
            }
        }
    }
}
