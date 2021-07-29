package com.vadimko.curforeckotlin

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vadimko.curforeckotlin.databinding.ActivityMainBinding


private const val CHANNEL_ID = "11"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel()
        checkConnection()
        //checkAutoUpdate()
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_now,
                R.id.navigation_today,
                R.id.navigation_dashboard,
                R.id.navigation_calc
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun checkAutoUpdate() {
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("updateon", false)
        if (!isServiceAlive(TCSupdateService::class.java) and pref) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this@MainActivity, TCSupdateService::class.java))
            } else {
                val i = Intent(this@MainActivity, TCSupdateService::class.java)
                this@MainActivity.startService(i)
            }
        }
    }

    /* private fun requestWorkingServices(): Boolean {
         val serviceActive: Boolean
         val am = this@MainActivity.getSystemService(ACTIVITY_SERVICE) as ActivityManager
         val rs = am.getRunningServices(50)
         serviceActive = rs.size != 0
         Log.wtf("reqWS", isServiceAlive(TCSupdateService::class.java).toString())
         return serviceActive
     }*/

    private fun isServiceAlive(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.namechannel)
            val descriptionText = getString(R.string.descriptionchannel)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun portraitMode(mainActivity: MainActivity) {
            val navView: BottomNavigationView = mainActivity.binding.navView
            navView.visibility = View.VISIBLE
            val toolbar = mainActivity.supportActionBar
            toolbar?.show()
            setFullscreen(false, mainActivity)
        }

        fun landscapeMode(mainActivity: MainActivity) {
            val navView: BottomNavigationView = mainActivity.binding.navView
            navView.visibility = View.GONE
            val toolbar = mainActivity.supportActionBar
            toolbar?.hide()
            setFullscreen(true, mainActivity)
        }

        private fun setFullscreen(fullscreen: Boolean, mainActivity: MainActivity) {
            val attrs = mainActivity.window.attributes
            if (fullscreen) {
                attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            } else {
                attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            }
            mainActivity.window.attributes = attrs
        }
    }

    override fun onResume() {
        val newConfig: android.content.res.Configuration = resources.configuration
        if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
            portraitMode(this)
        else landscapeMode(this)
        checkAutoUpdate()
        super.onResume()
    }

    private fun checkConnection() {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true)
            Toast.makeText(this, getString(R.string.noconnection), Toast.LENGTH_LONG).show()
    }

}