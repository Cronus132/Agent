package com.ledger.app.data

import kotlinx.coroutines.flow.Flow

/** Thin layer over the DAO so the rest of the app never touches Room directly. */
class TransactionRepository(private val dao: TransactionDao) {

    val transactions: Flow<List<Transaction>> = dao.observeAll()

    suspend fun add(transaction: Transaction) = dao.insert(transaction)

    suspend fun update(transaction: Transaction) = dao.update(transaction)

    suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    suspend fun clearAll() = dao.clearAll()
}
