package com.vadimko.curforeckotlin.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vadimko.curforeckotlin.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Utility object to check connection state
 */
object CheckConnection : KoinComponent {
    private var stopShowToast = false
    private val context: Context by inject()

    /**
     * Check internet connection state
     * @return false if no internet, otherwise return true
     */
    fun checkConnect(): Boolean {
        val connectivityManager =
            context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val connect = (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        if (connect != null) {
            return if (!connect) {
                blockSeveralToast()
                false
            } else
                true
        }
        blockSeveralToast()
        return false
    }

    /**
     * Block continuously showing several Toast at one moment
     */
    private fun blockSeveralToast() {
        if (!stopShowToast)
            Toast.makeText(context, context.getString(R.string.noconnection), Toast.LENGTH_LONG)
                .show()
        stopShowToast = true
        val handler = Handler(Looper.myLooper()!!)
        handler.postDelayed({ stopShowToast = false }, 1000)
    }
}