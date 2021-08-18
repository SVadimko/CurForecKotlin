package com.vadimko.curforeckotlin.utils

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Save/load in prefs spinner conditions on Today fragment
 */

object TodayPreferences : KoinComponent {

    private val context: Context by inject()

    fun savePrefs(
        request: String,
        from: String,
        till: String,
        interval: String,
        currSp: Int,
        perSp: Int,
        rateSp: Int
    ) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit {
            putString("request", request)
            putString("fromDate", from)
            putString("tillDate", till)
            putString("interval", interval)
            putString("currSP", currSp.toString())
            putString("perSP", perSp.toString())
            putString("ratSP", rateSp.toString())
            apply()
        }
    }

    fun loadPrefs(): List<String> {
        val till = Date(System.currentTimeMillis())
        val from = Date(System.currentTimeMillis() - 86400000 * 1)
        val result = DateConverter.getFromTillDate(from, till)
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
}