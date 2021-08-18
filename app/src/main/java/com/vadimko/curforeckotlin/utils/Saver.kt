package com.vadimko.curforeckotlin.utils

import android.content.Context
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.*


/**
 * Util class for save/load autoupdate data
 */

object Saver : KoinComponent {

    private val context: Context by inject()

    private val path: String = context.filesDir.path

    fun saveTcsLast(newData: MutableList<CurrencyTCS>) {
        val temp: MutableList<MutableList<CurrencyTCS>> = loadTcsLast()
        temp.add(newData)
        try {
            ObjectOutputStream(FileOutputStream(path + "TCSlast.sav")).use {
                it.writeObject(temp)
                CalcViewModel.loadServiceUpdateData()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadTcsLast(): MutableList<MutableList<CurrencyTCS>> {
        var tcsList: MutableList<MutableList<CurrencyTCS>> = mutableListOf()
        try {
            ObjectInputStream(FileInputStream(path + "TCSlast.sav")).use {
                tcsList = it.readObject() as MutableList<MutableList<CurrencyTCS>>
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return tcsList
    }

    fun deleteTcsLast(temp: List<List<CurrencyTCS>>) {
        //val temp: MutableList<MutableList<CurrencyTCS>> = mutableListOf()
        //temp.filter { it == temp.last() }
        //val delArray : MutableList<MutableList<CurrencyTCS>> = mutableListOf()
        val delArray = temp.filter { it == temp.last() }
        //delArray.add(temp.last() as MutableList<CurrencyTCS>)
        try {
            ObjectOutputStream(FileOutputStream(path + "TCSlast.sav")).use {
                it.writeObject(delArray)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}