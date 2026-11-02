package com.ledger.app

import android.app.Application
import com.ledger.app.data.AppDatabase
import com.ledger.app.data.TransactionRepository

/** Holds the single database + repository for the app's lifetime. */
class LedgerApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
