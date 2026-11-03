package com.ledger.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ledger.app.data.SavingsGoal
import com.ledger.app.ui.FinanceUiState
import com.ledger.app.ui.theme.incomeColor
import com.ledger.app.util.formatCents
import com.ledger.app.util.parseAmountToCents
import kotlin.math.roundToInt

@Composable
fun GoalsScreen(
    state: FinanceUiState,
    onAddGoal: (name: String, targetCents: Long) -> Unit,
    onContribute: (goal: SavingsGoal, deltaCents: Long) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit
) {
    var showAddGoal by remember { mutableStateOf(false) }
    var contributePair by remember { mutableStateOf<Pair<SavingsGoal, Boolean>?>(null) }
    var pendingDelete by remember { mutableStateOf<SavingsGoal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Total saved", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    formatCents(state.totalSavedCents, state.currency),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = incomeColor()
                )
            }
        }

        Button(onClick = { showAddGoal = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New savings goal")
        }

        if (state.goals.isEmpty()) {
            EmptyHint("No goals yet.\nCreate one to start setting money aside.")
        } else {
            state.goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    currency = state.currency,
                    onAdd = { contributePair = goal to false },
                    onWithdraw = { contributePair = goal to true },
                    onDelete = { pendingDelete = goal }
                )
            }
        }
    }

    if (showAddGoal) {
        AddGoalDialog(
            currency = state.currency,
            onDismiss = { showAddGoal = false },
            onConfirm = { name, target ->
                onAddGoal(name, target)
                showAddGoal = false
            }
        )
    }

    contributePair?.let { (goal, isWithdraw) ->
        ContributeDialog(
            goal = goal,
            currency = state.currency,
            isWithdraw = isWithdraw,
            onDismiss = { contributePair = null },
            onConfirm = { cents ->
                onContribute(goal, if (isWithdraw) -cents else cents)
                contributePair = null
            }
        )
    }

    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete goal?") },
            text = { Text("\"${goal.name}\" and its saved progress will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteGoal(goal)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    currency: String,
    onAdd: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit
) {
    val fraction = if (goal.targetCents > 0) goal.savedCents.toFloat() / goal.targetCents.toFloat() else 0f
    val pct = if (goal.targetCents > 0) (goal.savedCents * 100.0 / goal.targetCents).roundToInt() else 0

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    goal.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            LabeledProgress(
                leading = "$pct% saved",
                trailing = formatCents(goal.savedCents, currency) +
                    " / " + formatCents(goal.targetCents, currency),
                fraction = fraction,
                color = incomeColor()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
                OutlinedButton(onClick = onWithdraw, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Withdraw")
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetCents: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    val targetCents = parseAmountToCents(target)
    val canSave = name.isNotBlank() && targetCents != null && targetCents > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New savings goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { input -> target = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Target amount") },
                    prefix = { Text(currency) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onConfirm(name, targetCents ?: 0L) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ContributeDialog(
    goal: SavingsGoal,
    currency: String,
    isWithdraw: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (cents: Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val cents = parseAmountToCents(amount)
    val canSave = cents != null && cents > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text((if (isWithdraw) "Withdraw from " else "Add to ") + goal.name) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter { it.isDigit() || it == '.' } },
                label = { Text("Amount") },
                prefix = { Text(currency) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onConfirm(cents ?: 0L) }
            ) { Text(if (isWithdraw) "Withdraw" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
