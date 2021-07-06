package com.vadimko.curforeckotlin

import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import java.io.*


//вспомогательный класс для сохранения/загрузки данных автообновления курса
class Saver {

     fun saveTCSlast(newdata:MutableList<CurrencyTCS>) {
         val temp: MutableList<MutableList<CurrencyTCS>> = loadTcslast()
        temp.add(newdata)
        try {
            ObjectOutputStream(FileOutputStream("/data/user/0/com.vadimko.curforeckotlin/files" + "TCSlast.sav")).use { it ->
                it.writeObject(temp)
                CalcViewModel.loadGraphData()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

     fun loadTcslast(): MutableList<MutableList<CurrencyTCS>> {
        var temp: MutableList<MutableList<CurrencyTCS>> = mutableListOf()
        try {
            ObjectInputStream(FileInputStream("/data/user/0/com.vadimko.curforeckotlin/files" + "TCSlast.sav")).use { it ->
                temp = it.readObject() as MutableList<MutableList<CurrencyTCS>>
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return temp
    }

    fun deleteTcslast(){
        val temp: List<CurrencyTCS> = listOf()
        try {
            ObjectOutputStream(FileOutputStream("/data/user/0/com.vadimko.curforeckotlin/files" + "TCSlast.sav")).use { it ->
                it.writeObject(temp)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}