package com.vadimko.curforeckotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vadimko.curforeckotlin.databinding.ActivityMainBinding
import com.vadimko.curforeckotlin.updateWorkers.ServiceCheckWorker
import com.vadimko.curforeckotlin.utils.CheckTCSUpdateServiceIsRunning
import com.vadimko.curforeckotlin.utils.ScopeCreator
import com.vadimko.curforeckotlin.utils.SoundPlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * Const of Channel ID for Notification channel
 */
private const val CHANNEL_ID = "11"

/**
 * Tag of ServiceCheckWorker
 */
private const val SERVICE_WORKER_TAG = "serviceChecker"

/**
 * MainActivity class
 */
class MainActivity : AppCompatActivity(), KoinComponent {
    private val scopeCreator: ScopeCreator by inject()
    private val context: Context by inject()

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel()
        SoundPlayer.onInit()
        scopeCreator.createScope()
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


    override fun onResume() {
        val newConfig: android.content.res.Configuration = resources.configuration
        if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
            portraitMode(this)
        else landscapeMode(this)
        CheckTCSUpdateServiceIsRunning.checkAutoUpdate()
        checkAutoUpdatePeriodic()
        super.onResume()
    }

    override fun onDestroy() {
        SoundPlayer.onDestroy()
        scopeCreator.cancelScope()
        super.onDestroy()
    }

    /**
     * If prefs auto update be [TCSUpdateService] is on start [ServiceCheckWorker] which periodicaly
     * checks is service alive and restart it, if no
     */
    private fun checkAutoUpdatePeriodic() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(SERVICE_WORKER_TAG)
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("updateon", false)
        if (pref) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val serviceChecker = PeriodicWorkRequest.Builder(
                ServiceCheckWorker::class.java, 20, TimeUnit.MINUTES, 15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(SERVICE_WORKER_TAG)
                .build()
            workManager.enqueue(serviceChecker)
        }
    }


    /**
     * Contains function to switch interface according landscape/portrait mode
     */
    companion object {

        /**
         * Show [BottomNavigationView] in portrait mode
         */
        fun portraitMode(mainActivity: MainActivity) {
            val navView: BottomNavigationView = mainActivity.binding.navView
            navView.visibility = View.VISIBLE
            val toolbar = mainActivity.supportActionBar
            toolbar?.show()
            setFullscreen(false, mainActivity)
        }

        /**
         * Hide [BottomNavigationView] in landscape mode
         */
        fun landscapeMode(mainActivity: MainActivity) {
            val navView: BottomNavigationView = mainActivity.binding.navView
            navView.visibility = View.GONE
            val toolbar = mainActivity.supportActionBar
            toolbar?.hide()
            setFullscreen(true, mainActivity)
        }

        /**
         * Toggles on fullscreen mode in landscape mode and off in portrait
         */
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
}