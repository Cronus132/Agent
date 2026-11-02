package com.ledger.app.util

import kotlin.math.absoluteValue

/** Formats a cents amount like 123456 -> "$1,234.56". */
fun formatCents(cents: Long, symbol: String): String {
    val negative = cents < 0
    val abs = cents.absoluteValue
    val whole = abs / 100
    val frac = (abs % 100).toInt()
    val sign = if (negative) "-" else ""
    return "$sign$symbol${groupThousands(whole)}.${frac.toString().padStart(2, '0')}"
}

private fun groupThousands(value: Long): String {
    val digits = value.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in digits.length - 1 downTo 0) {
        sb.append(digits[i])
        count++
        if (count % 3 == 0 && i != 0) sb.append(',')
    }
    return sb.reverse().toString()
}

/**
 * Parses user input such as "12.5" or "1,200" into cents.
 * Returns null when the text is empty or not a valid non-negative number.
 */
fun parseAmountToCents(input: String): Long? {
    val cleaned = input.trim().replace(",", "")
    if (cleaned.isEmpty()) return null
    val value = cleaned.toDoubleOrNull() ?: return null
    if (value < 0) return null
    return Math.round(value * 100)
}
