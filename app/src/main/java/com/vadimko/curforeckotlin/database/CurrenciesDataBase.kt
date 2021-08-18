package com.vadimko.curforeckotlin.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Currencies::class], version = 2, exportSchema = false)
@TypeConverters(CurrenciesTypeConverter::class)
abstract class CurrenciesDataBase : RoomDatabase() {
    abstract fun currenciesDao(): CurrencyDao
}