package com.ledger.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single income or expense entry.
 *
 * Amounts are stored as whole cents (minor units) in a [Long] so money math is
 * always exact — no floating point rounding errors.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
