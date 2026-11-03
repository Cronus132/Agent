package com.ledger.app.data

import android.content.Context

/** Stores simple app settings (currency symbol, monthly budget) locally. */
class CurrencyPreference(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    var symbol: String
        get() = prefs.getString(KEY_SYMBOL, DEFAULT) ?: DEFAULT
        set(value) { prefs.edit().putString(KEY_SYMBOL, value).apply() }

    /** Monthly spending budget in cents. 0 means "no budget set". */
    var monthlyBudgetCents: Long
        get() = prefs.getLong(KEY_BUDGET, 0L)
        set(value) { prefs.edit().putLong(KEY_BUDGET, value).apply() }

    companion object {
        private const val KEY_SYMBOL = "currency_symbol"
        private const val KEY_BUDGET = "monthly_budget_cents"
        const val DEFAULT = "$"
    }
}
