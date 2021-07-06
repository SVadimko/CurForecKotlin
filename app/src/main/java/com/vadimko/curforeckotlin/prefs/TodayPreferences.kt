package com.vadimko.curforeckotlin.prefs

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.DateConverter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//сохранение/загрузка состояния спиннеров для графика на фрагменте Today
object TodayPreferences {

    fun savePrefs(
        context: Context,
        request: String,
        from: String,
        till: String,
        interval: String,
        currSp: Int,
        perSp: Int,
        rateSp: Int
    ) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val preferencesEditor = sp.edit()
        preferencesEditor.putString("request", request)
        preferencesEditor.putString("fromDate", from)
        preferencesEditor.putString("tillDate", till)
        preferencesEditor.putString("interval", interval)
        preferencesEditor.putString("currSP", currSp.toString())
        preferencesEditor.putString("perSP", perSp.toString())
        preferencesEditor.putString("ratSP", rateSp.toString())
        preferencesEditor.apply()
    }

    fun loadPrefs(context: Context): List<String> {
        //val result = dateConverter(1, context)
        val till = Date(System.currentTimeMillis())
        val from = Date(System.currentTimeMillis() - 86400000 * 1)
        val result = DateConverter.getFromTillDate(from, till, context)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val data1 = sp.getString("request", "USD000000TOD")!!
        val data2 = sp.getString("fromDate", result[0][0])!!
        val data3 = sp.getString("tillDate", result[0][1])!!
        val data4 = sp.getString("interval", "10")!!
        val data5 = sp.getString("currSP", "0")!!
        val data6 = sp.getString("perSP", "0")!!
        val data7 = sp.getString("ratSP", "0")!!
        return listOf(data1, data2, data3, data4, data5, data6, data7)
    }

    private fun dateConverter(days: Long, context: Context): ArrayList<Array<String>> {
        val result = ArrayList<Array<String>>()
        val timeMilli = System.currentTimeMillis()
        val timeWeekAgo = timeMilli - 86400000 * days
        val dateRes1 = Date(timeMilli)
        val dateRes2 = Date(timeWeekAgo)
        val jdf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd", context.resources.configuration.locales[0])
        } else {
            SimpleDateFormat("yyyy-MM-dd", context.resources.configuration.locale)
        }
        val res = arrayOf(jdf.format(dateRes2), jdf.format(dateRes1))
        result.add(0, res)
        val jdf2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locales[0])
        } else {
            SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locale)
        }
        val res2 = arrayOf(jdf2.format(dateRes2), jdf2.format(dateRes1))
        result.add(1, res2)
        return result
    }
}