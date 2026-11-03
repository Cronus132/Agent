package com.ledger.app.data

import kotlinx.coroutines.flow.Flow

/** Single access point for all persisted data: transactions and savings goals. */
class LedgerRepository(
    private val transactionDao: TransactionDao,
    private val goalDao: SavingsGoalDao
) {
    // Transactions
    val transactions: Flow<List<Transaction>> = transactionDao.observeAll()

    suspend fun add(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)
    suspend fun clearAll() = transactionDao.clearAll()

    // Savings goals
    val goals: Flow<List<SavingsGoal>> = goalDao.observeAll()

    suspend fun addGoal(goal: SavingsGoal) = goalDao.insert(goal)
    suspend fun updateGoal(goal: SavingsGoal) = goalDao.update(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = goalDao.delete(goal)
}
