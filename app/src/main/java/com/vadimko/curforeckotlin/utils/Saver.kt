package com.vadimko.curforeckotlin.utils

import android.content.Context
import com.vadimko.curforeckotlin.TCSUpdateService
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.utils.Saver.context
import com.vadimko.curforeckotlin.utils.Saver.path
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.*


/**
 * Util class for save/load autoupdate receiver from [TCSUpdateService]data
 * @property context Application context injected by Koin
 * @property path get files directory path
 */

const val FILENAME = "TCSlast.sav"

object Saver : KoinComponent {


    private val context: Context by inject()

    private val path: String = context.filesDir.path

    /**
     * Load previous values add new value [newData] to it and save them in storage
     */
    fun saveTcsLast(newData: MutableList<CurrencyTCS>) {
        val temp: MutableList<MutableList<CurrencyTCS>> = loadTcsLast()
        temp.add(newData)
        try {
            ObjectOutputStream(FileOutputStream(path + FILENAME)).use {
                it.writeObject(temp)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    /**
     * Load stored values
     */
    @Suppress("UNCHECKED_CAST")
    fun loadTcsLast(): MutableList<MutableList<CurrencyTCS>> {
        var tcsList: MutableList<MutableList<CurrencyTCS>> = mutableListOf()
        try {
            ObjectInputStream(FileInputStream(path + FILENAME)).use {
                tcsList = it.readObject() as MutableList<MutableList<CurrencyTCS>>
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return tcsList
    }

    /**
     * Delete all values, except last one
     */
    fun deleteTcsLast(temp: List<List<CurrencyTCS>>) {
        val delArray = temp.filter { it == temp.last() }
        try {
            ObjectOutputStream(FileOutputStream(path + FILENAME)).use {
                it.writeObject(delArray)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}