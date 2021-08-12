package com.vadimko.curforeckotlin

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
                CalcViewModel.loadGraphData()
                it.close()
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
                it.close()
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
                it.close()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}