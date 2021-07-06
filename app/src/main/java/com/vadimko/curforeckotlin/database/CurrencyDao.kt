package com.vadimko.curforeckotlin.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currencies")
    fun getCurrencies(): LiveData<List<Currencies>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCurrencies(currencies: Currencies)

    @Delete
    fun clearCurrencies(list: List<Currencies>)
}