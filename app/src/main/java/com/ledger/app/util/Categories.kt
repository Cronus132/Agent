package com.ledger.app.util

import com.ledger.app.data.TransactionType

/** Fixed category lists for the add-transaction picker. */
object Categories {
    val expense = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Entertainment", "Other")
    val income = listOf("Salary", "Business", "Gift", "Investment", "Other")

    fun forType(type: TransactionType): List<String> =
        if (type == TransactionType.INCOME) income else expense
}
