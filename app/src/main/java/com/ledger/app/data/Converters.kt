package com.ledger.app.data

import androidx.room.TypeConverter

/** Lets Room persist the [TransactionType] enum as its name. */
class Converters {
    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromType(type: TransactionType): String = type.name
}
