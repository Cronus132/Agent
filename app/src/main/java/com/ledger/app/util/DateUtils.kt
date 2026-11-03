package com.ledger.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Midnight (local time) of the day containing [millis]. */
fun startOfDay(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** "Today" / "Yesterday" / "Mon, 3 Nov 2025" for a day-start timestamp. */
fun dayLabel(dayStart: Long): String {
    val today = startOfDay(System.currentTimeMillis())
    val yesterday = today - DAY_MILLIS
    return when (dayStart) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(dayStart))
    }
}

fun timeLabel(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

/** Midnight (local time) on the first day of the month containing [millis]. */
fun startOfMonth(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** "November 2026" for a timestamp. */
fun monthLabel(millis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(millis))

/**
 * The Material date picker reports the selected day as UTC midnight. Convert that
 * to local noon on the same calendar day so day-grouping stays correct everywhere.
 */
fun pickedDateToLocalMillis(utcMidnightMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMillis }
    val local = Calendar.getInstance()
    local.set(
        utc.get(Calendar.YEAR),
        utc.get(Calendar.MONTH),
        utc.get(Calendar.DAY_OF_MONTH),
        12, 0, 0
    )
    local.set(Calendar.MILLISECOND, 0)
    return local.timeInMillis
}

private const val DAY_MILLIS = 24L * 60 * 60 * 1000
