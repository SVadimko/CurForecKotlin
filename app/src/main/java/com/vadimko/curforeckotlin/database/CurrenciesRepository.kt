package com.vadimko.curforeckotlin.database

import android.content.Context
import androidx.room.Room
import com.vadimko.curforeckotlin.utils.ScopeCreator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val DATABASE_NAME = "currencies-database"

/**
 * Repository for work with DataBase
 * @property stopWrite - flag used for blocking several continuous write to database, when at the
 * same time updating several widgets
 */

class CurrenciesRepository constructor(context: Context) : KoinComponent {
    private val scopeCreator: ScopeCreator by inject()
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
     * @return Flow of list [Currencies]
     */
    fun getCurrencies(): Flow<List<Currencies>> = currencyDao.getCurrencies()


    /**
     * Add [Currencies] value to DB if [stopWrite] allows it
     */
    fun insertCurrencies(currencies: Currencies) {
        scopeCreator.getScope().launch {
            if (!stopWrite) {
                currencyDao.addCurrencies(currencies)
            }
            stopWrite = true
            launch {
                delay(60000)
                stopWrite = false
            }
        }
    }

    /**
     * Delete all [Currencies] except last in DB
     */
    fun clearCurrencies(list: MutableList<Currencies>) {
        scopeCreator.getScope().launch {
            if (!list.isNullOrEmpty()) {
                list.removeLast()
                currencyDao.clearCurrencies(list)
            }
        }
    }


    /**
     * Drop table (not used)
     */
    fun dropTable() {
        currencyDao.nukeTable()
    }

    /**
     * Initialise and get singleton of [CurrenciesRepository] DB (not used, replaced by inject Koin)
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