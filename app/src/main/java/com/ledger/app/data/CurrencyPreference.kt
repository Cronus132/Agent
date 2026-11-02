package com.ledger.app.data

import android.content.Context

/** Stores the chosen currency symbol locally in SharedPreferences. */
class CurrencyPreference(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    var symbol: String
        get() = prefs.getString(KEY, DEFAULT) ?: DEFAULT
        set(value) { prefs.edit().putString(KEY, value).apply() }

    companion object {
        private const val KEY = "currency_symbol"
        const val DEFAULT = "$"
    }
}
