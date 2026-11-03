package com.ledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.expenseColor
import com.ledger.app.ui.theme.incomeColor
import com.ledger.app.util.formatCents
import com.ledger.app.util.monthLabel

@Composable
fun DashboardScreen(state: FinanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BalanceCard(state)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

        MonthCard(state)

        Text("Recent", style = MaterialTheme.typography.titleSmall)

        if (state.recent.isEmpty()) {
            EmptyHint("No transactions yet.\nTap + to add your first one.")
        } else {
            state.recent.forEach { transaction ->
                TransactionRow(transaction, state.currency)
            }
        }
    }
}

@Composable
private fun MonthCard(state: FinanceUiState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                monthLabel(System.currentTimeMillis()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MonthStat("Income", formatCents(state.monthIncomeCents, state.currency), incomeColor())
                MonthStat("Spent", formatCents(state.monthExpenseCents, state.currency), expenseColor())
                MonthStat(
                    "Net",
                    formatCents(state.monthNetCents, state.currency),
                    if (state.monthNetCents < 0) expenseColor() else incomeColor(),
                    alignEnd = true
                )
            }

            if (state.monthlyBudgetCents > 0) {
                Spacer(Modifier.height(14.dp))
                val remaining = state.monthlyBudgetCents - state.monthExpenseCents
                val overBudget = remaining < 0
                LabeledProgress(
                    leading = if (overBudget) "Over budget" else "Budget left",
                    trailing = formatCents(remaining, state.currency) +
                        " / " + formatCents(state.monthlyBudgetCents, state.currency),
                    fraction = state.monthExpenseCents.toFloat() /
                        state.monthlyBudgetCents.toFloat(),
                    color = if (overBudget) expenseColor() else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MonthStat(label: String, value: String, color: androidx.compose.ui.graphics.Color, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}
