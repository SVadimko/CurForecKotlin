package com.vadimko.curforeckotlin.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.util.concurrent.Executors

private const val DATABASE_NAME = "currencies-database"

/**
 * repository for work with DataBase
 */

class CurrenciesRepository private constructor(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val database: CurrenciesDataBase = Room.databaseBuilder(
        context.applicationContext,
        CurrenciesDataBase::class.java,
        DATABASE_NAME
    )
        //.allowMainThreadQueries()
        .build()

    private val currencyDao = database.currenciesDao()


    fun getCurrencies(): LiveData<List<Currencies>> = currencyDao.getCurrencies()

    fun insertCurrencies(currencies: Currencies) {
        executor.execute {
            currencyDao.addCurrencies(currencies)
        }
    }

    fun clearCurrencies(list: List<Currencies>) {
        executor.execute {
            currencyDao.clearCurrencies(list)
        }
    }

    companion object {
        private var INSTANCE: CurrenciesRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CurrenciesRepository(context)
            }
        }

        fun get(): CurrenciesRepository {
            return INSTANCE
                ?: throw IllegalStateException("CurrenciesRepository must be initialized")
        }
    }


}