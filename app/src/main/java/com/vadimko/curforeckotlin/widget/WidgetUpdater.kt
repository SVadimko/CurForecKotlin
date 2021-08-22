package com.vadimko.curforeckotlin.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import com.vadimko.curforeckotlin.cbjsonApi.CBjsonRepository
import com.vadimko.curforeckotlin.tcsApi.TCSRepository

/**
 * Perform request to servers to get Tinkov and CB data
 * @param appWidgetID Id of updating widget
 * @param appWidgetManager Application widget Manager
 */

class WidgetUpdater(context: Context, appWidgetManager: AppWidgetManager, appWidgetID: Int) {
    private val mappWidgetManager = appWidgetManager
    private val mappWidgetID = appWidgetID

    init {
        updateTCs()
        updateCB()
    }

    /**
     * Performs Retrofit [TCSRepository] request to Tinkov server
     */
    private fun updateTCs() {
        val tcsRepository = TCSRepository(true, mappWidgetManager, mappWidgetID)
        tcsRepository.getCurrentTCS()
    }

    /**
     * Performs Retrofit [CBjsonRepository] request to CB server
     */
    private fun updateCB() {
        val cbJsonRepository = CBjsonRepository(true, mappWidgetManager, mappWidgetID)
        cbJsonRepository.getCurrentCB()
    }
}
