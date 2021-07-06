package com.vadimko.curforeckotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

//бутресивер для запуска сервиса автообновления курса валют Тиньков после перезагрузки, если в настройках приложения выбрано автообновление
class BootReceiver : BroadcastReceiver() {
    lateinit var mContext: Context


    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(mContext)
            .getBoolean("updateon", false)
        if (pref)
            startUpdater()
    }

    private fun startUpdater() {
        try {
            val serviceIntent = Intent(mContext, TCSupdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //val serviceIntent = Intent(mContext, TCSupdateService::class.java)
                mContext.startForegroundService(serviceIntent)
            } else {
                //val serviceIntent = Intent(mContext, TCSSimpleService::class.java)
                mContext.startService(serviceIntent)
            }
        } catch (ex: Throwable) {
        }
    }
}