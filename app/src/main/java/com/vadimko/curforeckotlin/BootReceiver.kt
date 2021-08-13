package com.vadimko.curforeckotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Bootreceiver for starting the service of auto-update of the exchange rate Tinkov after reboot,
 * if auto-update is selected in the application settings
 */

class BootReceiver : BroadcastReceiver() {
    lateinit var mContext: Context

    /**
     * If auto update enabled - launch [startUpdater]
     */
    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(mContext)
            .getBoolean("updateon", false)
        if (pref)
            startUpdater()
    }

    /**
     * Depends on android version start foreground or background [TCSUpdateService]
     */
    private fun startUpdater() {
        try {
            val serviceIntent = Intent(mContext, TCSUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(serviceIntent)
            } else {
                mContext.startService(serviceIntent)
            }
        } catch (ex: Throwable) {
        }
    }
}