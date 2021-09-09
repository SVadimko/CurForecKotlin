package com.vadimko.curforeckotlin.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currencies")
    fun getCurrencies(): Flow<List<Currencies>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCurrencies(currencies: Currencies)

    @Delete
    suspend fun clearCurrencies(list: List<Currencies>)

    @Query("DELETE FROM currencies")
    fun nukeTable()
}