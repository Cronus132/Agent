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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.expenseColor
import com.ledger.app.ui.theme.incomeColor
import com.ledger.app.util.formatCents
import com.ledger.app.util.monthLabel
import kotlin.math.roundToInt

@Composable
fun ReportsScreen(state: FinanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(monthLabel(System.currentTimeMillis()), style = MaterialTheme.typography.titleSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryTile("Income", state.monthIncomeCents, incomeColor(), state.currency, Modifier.weight(1f))
            SummaryTile("Expenses", state.monthExpenseCents, expenseColor(), state.currency, Modifier.weight(1f))
            SummaryTile(
                "Saved",
                state.monthNetCents,
                if (state.monthNetCents < 0) expenseColor() else incomeColor(),
                state.currency,
                Modifier.weight(1f)
            )
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                LabeledProgress(
                    leading = "Savings rate",
                    trailing = "${(state.savingsRate * 100).roundToInt()}%",
                    fraction = state.savingsRate,
                    color = incomeColor()
                )
                if (state.monthlyBudgetCents > 0) {
                    Spacer(Modifier.height(14.dp))
                    val spent = state.monthExpenseCents
                    val budget = state.monthlyBudgetCents
                    val over = spent > budget
                    LabeledProgress(
                        leading = if (over) "Over budget" else "Budget used",
                        trailing = formatCents(spent, state.currency) +
                            " / " + formatCents(budget, state.currency),
                        fraction = spent.toFloat() / budget.toFloat(),
                        color = if (over) expenseColor() else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text("Spending by category", style = MaterialTheme.typography.titleSmall)

        if (state.categorySpend.isEmpty()) {
            EmptyHint("No spending recorded this month yet.")
        } else {
            val total = state.monthExpenseCents.coerceAtLeast(1)
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.categorySpend.forEach { entry ->
                        val pct = (entry.cents * 100.0 / total).roundToInt()
                        LabeledProgress(
                            leading = "${entry.category}  ·  $pct%",
                            trailing = formatCents(entry.cents, state.currency),
                            fraction = entry.cents.toFloat() / total.toFloat(),
                            color = expenseColor()
                        )
                    }
                }
            }
        }

        Text(
            "Totals cover ${monthLabel(System.currentTimeMillis())}.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
    }
}
