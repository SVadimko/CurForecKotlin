package com.vadimko.curforeckotlin.widget

import android.appwidget.AppWidgetManager
import com.vadimko.curforeckotlin.cbjsonApi.CBjsonRepository
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import org.koin.core.component.KoinComponent

/**
 * Perform request to servers to get Tinkov and CB data
 * @param appWidgetID Id of updating widget
 * @param appWidgetManager Application widget Manager
 */

class WidgetUpdater(appWidgetManager: AppWidgetManager, appWidgetID: Int) : KoinComponent {
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
        val tcsRepository = TCSRepository()
        tcsRepository.getCurrentTCS(true, mappWidgetManager, mappWidgetID)
    }

    /**
     * Performs Retrofit [CBjsonRepository] request to CB server
     */
    private fun updateCB() {
        val cbJsonRepository = CBjsonRepository()
        cbJsonRepository.getCurrentCB(true, mappWidgetManager, mappWidgetID)
    }
}
