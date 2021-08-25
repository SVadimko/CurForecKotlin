package com.vadimko.curforeckotlin.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.vadimko.curforeckotlin.TCSUpdateService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Utility object to check is [TCSUpdateService] is alive if it was enabled in SharedPrefs
 */
object CheckTCSUpdateServiceIsRunning : KoinComponent {

    private val context: Context by inject()

    /**
     * Function checks is [TCSUpdateService] is alive if it was enabled in SharedPrefs
     * via [isServiceAlive] and launch it if it was killed
     */
    fun checkAutoUpdate() {
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("updateon", false)
        if (!isServiceAlive(TCSUpdateService::class.java) and pref) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(
                        context,
                        TCSUpdateService::class.java
                    )
                )
            } else {
                val i = Intent(context, TCSUpdateService::class.java)
                context.startService(i)
            }
        }
    }

    /**
     * Checking is wanted service is alive
     */
    private fun isServiceAlive(serviceClass: Class<*>): Boolean {
        val manager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
