package com.vadimko.curforeckotlin

import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import java.io.*


/**
 * util class for save/load auto update data
 */

class Saver {
    val path: String = CurrenciesApplication.applicationContext().filesDir.path

    fun saveTcsLast(newData: MutableList<CurrencyTCS>) {
        val temp: MutableList<MutableList<CurrencyTCS>> = loadTcsLast()
        temp.add(newData)
        try {
            ObjectOutputStream(FileOutputStream(path + "TCSlast.sav")).use {
                it.writeObject(temp)
                CalcViewModel.loadGraphData()
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

    fun deleteTcsLast() {
        val temp: List<CurrencyTCS> = listOf()
        try {
            ObjectOutputStream(FileOutputStream(path + "TCSlast.sav")).use {
                it.writeObject(temp)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}