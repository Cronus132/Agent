package com.ledger.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A savings target the user is putting money aside for (e.g. "Emergency fund").
 * [savedCents] is tracked independently of the main balance — it's money the user
 * marks as set aside, so goals act as separate "pots".
 */
@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetCents: Long,
    val savedCents: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
