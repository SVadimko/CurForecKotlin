package com.vadimko.curforeckotlin.utils

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.utils.ArchivePreferences.context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Save/load request to server of chosen data on Archive fragment
 * @property context Application context injected by Koin
 */

object ArchivePreferences : KoinComponent {

    private val context: Context by inject()

    /**
     * Saving in SharedPreferences last request on ArchiveFragment
     */
    fun savePrefs(
        from: Long,
        till: Long,
        currSelecter: Int,
        VAL_NM_RQ: String,
        date_rec1: String,
        date_rec2: String,
        request: String,
        fromMoex: String,
        tillMoex: String,
        interval: String
    ) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit {
            putLong("fromARDate", from)
            putLong("tillARDate", till)
            putInt("currSelecter", currSelecter)
            putString("VAL_NM_RQ", VAL_NM_RQ)
            putString("date_rec1CB", date_rec1)
            putString("date_rec2CB", date_rec2)
            putString("requestARMOEX", request)
            putString("fromMOEX", fromMoex)
            putString("tillMOEX", tillMoex)
            putString("intervalARMOEX", interval)
            apply()
        }
    }

    /**
     * Loading from SharedPreferences last request on ArchiveFragment
     */
    fun loadPrefs(): List<String> {
        val from = Date(System.currentTimeMillis() - 604800000)
        val till = Date(System.currentTimeMillis())
        val result = DateConverter.getFromTillDate(from, till)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val data0 = sp.getLong("fromARDate", System.currentTimeMillis() - 604800000).toString()
        val data1 = sp.getLong("tillARDate", System.currentTimeMillis()).toString()
        val data2 = sp.getInt("currSelecter", 0).toString()
        val data3 = sp.getString("VAL_NM_RQ", "R01235")!!
        val data4 = sp.getString("date_rec1CB", result[1][0])!!
        val data5 = sp.getString("date_rec2CB", result[1][1])!!
        val data6 = sp.getString("requestARMOEX", "USD000000TOD")!!
        val data7 = sp.getString("fromMOEX", result[0][0])!!
        val data8 = sp.getString("tillMOEX", result[0][1])!!
        val data9 = sp.getString("intervalARMOEX", "24")!!
        return listOf(data0, data1, data2, data3, data4, data5, data6, data7, data8, data9)
    }
}