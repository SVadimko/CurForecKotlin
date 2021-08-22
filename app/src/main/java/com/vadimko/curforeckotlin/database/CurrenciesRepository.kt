package com.vadimko.curforeckotlin.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private const val DATABASE_NAME = "currencies-database"

/**
 * Repository for work with DataBase
 * @property stopWrite - flag used for blocking several continuous write to database, when at the
 * same time updating several widgets
 */

class CurrenciesRepository private constructor(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val database: CurrenciesDataBase = Room.databaseBuilder(
        context.applicationContext,
        CurrenciesDataBase::class.java,
        DATABASE_NAME
    )
        //.allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()

    private val currencyDao = database.currenciesDao()

    private var stopWrite = false

    /**
     * @return Livedata of list [Currencies]
     */
    fun getCurrencies(): LiveData<List<Currencies>> = currencyDao.getCurrencies()

    /**
     * Add [Currencies] value to DB if [stopWrite] allows it
     */
    fun insertCurrencies(currencies: Currencies) {

        executor.execute {
            if (!stopWrite) {
                currencyDao.addCurrencies(currencies)
            }
            stopWrite = true
            GlobalScope.launch(Dispatchers.IO) {
                delay(60000)
                stopWrite = false
            }
        }
    }

    /**
     * Delete all [Currencies] except last in DB
     */
    fun clearCurrencies(list: MutableList<Currencies>) {
        executor.execute {
            list.removeLast()
            currencyDao.clearCurrencies(list)
        }
    }

    /**
     * Drop table (not used)
     */
    fun dropTable() {
        currencyDao.nukeTable()
    }

    /**
     * Initialise and get singleton of [CurrenciesRepository] DB
     */
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