package com.vadimko.curforeckotlin

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import com.vadimko.curforeckotlin.utils.CheckConnection
import com.vadimko.curforeckotlin.utils.Saver
import com.vadimko.curforeckotlin.utils.ScopeCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "11"
var NOTIFICATION_ID = 5555
private const val notificationId = 11

/**
 * Service for auto-updating the rates of the Tinkov bank
 */
class TCSUpdateService : Service(), KoinComponent {
    private val tcsRepository: TCSRepository by inject()
    private val scopeCreator: ScopeCreator by inject()
    private var period: Long = 5


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateTask()
        startForeground(
            NOTIFICATION_ID,
            buildForegroundNotification(
                "USD:  ?",
                "EUR  ?| GBP  ?"
            )
        )
        return START_REDELIVER_INTENT
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

    private fun updateTask() {
        scopeCreator.getScope().launch(Dispatchers.IO) {
            var allRequest = 1
            while (true) {
                period = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    .getString("update_per", "15_min")?.split("_")?.get(0)?.toLong()!!
                if (CheckConnection.checkConnect()) getCurrentTCS()
                for (y in 0 until period * 12) {
                    try {
                        TimeUnit.SECONDS.sleep(5)
                        if (!PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                .getBoolean("updateon", false)
                        ) {
                            val notificationManager =
                                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancelAll()
                            break
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                if (!PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        .getBoolean("updateon", false)
                ) {
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()
                    break
                }
                allRequest++
            }
            stopSelf()
        }//.start()
    }

    /**
     * Performs request to Tinkov server through Retrofit [tcsRepository] and then saved it with [Saver]
     * and post value to [CalcViewModel] dataServiceUpdate
     */
    private fun getCurrentTCS() {
        scopeCreator.getScope().launch {
            var list: List<CurrencyTCS>
            do {
                list = tcsRepository.getResponse()
            } while (list.size != 3)
            scopeCreator.getScope().launch(Dispatchers.IO) {
                Saver.saveTcsLast(list as MutableList<CurrencyTCS>)
                CalcViewModel.loadServiceUpdateData()
            }
            val usdBuy = String.format("%.2f", list[0].buy)
            val usdSell = String.format("%.2f", list[0].sell)
            val eurBuy = String.format("%.2f", list[1].buy)
            val eurSell = String.format("%.2f", list[1].sell)
            val gbpBuy = String.format("%.2f", list[2].buy)
            val gbpSell = String.format("%.2f", list[2].sell)
            startForeground(
                NOTIFICATION_ID,
                buildForegroundNotification(
                    "USD: $usdBuy - $usdSell",
                    "EUR $eurBuy $eurSell | GBP $gbpBuy $gbpSell"
                )
            )
            checkCurrencyLevel(list)
        }
    }


    private fun buildForegroundNotification(s: String, s2: String): Notification {
        val b = NotificationCompat.Builder(this, CHANNEL_ID)
        b.setOngoing(true)
            .setUsesChronometer(true)
            .setColorized(true)
            .setContentTitle(s)
            .setContentText(s2)
            .setSmallIcon(R.drawable.sign)
            .setTicker(s2)
        val startAPP = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(startAPP)
        val pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        b.setContentIntent(pIntent)

        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, b.build())
        return b.build()
    }

    /**
     * Read user settings which currency and values need to monitor
     */
    private fun checkCurrencyLevel(dataList: List<CurrencyTCS>) {
        val notifyCheck =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean("notify", false)
        val notifyCurr =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("currency", "USD")
        val notifyBuy =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("buymore", "30.0")
        val notifySell =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("sellmore", "100.0")
        if (notifyCheck)
            checking(notifyCurr, notifyBuy, notifySell, dataList)
    }

    /**
     * Comparing actual currency values to values that were set by user and start notification
     */
    private fun checking(
        curr: String?,
        buyLevel: String?,
        sellLevel: String?,
        dataList: List<CurrencyTCS>
    ) {
        lateinit var currencyTCS: CurrencyTCS
        when (curr) {
            "USD" -> {
                currencyTCS = dataList[0]
            }
            "EUR" -> {
                currencyTCS = dataList[1]
            }
            "GBP" -> {
                currencyTCS = dataList[2]
            }
        }
        val buySet = buyLevel?.toDouble()
        val sellSet = sellLevel?.toDouble()
        if (buySet != null) {
            if (buySet > 0.0) {
                if (buySet > currencyTCS.buy!!) {
                    val notifyText = "${applicationContext.getString(R.string.buycourse)} $curr ${
                        applicationContext.getString(R.string.higher)
                    } $buySet₽ (${currencyTCS.buy}₽)"
                    notification(notifyText)
                }
            }
        }
        if (sellSet != null) {
            if (sellSet > 0.0) {
                if (sellSet > currencyTCS.sell!!) {
                    val notifyText = "${applicationContext.getString(R.string.sellcourse)} $curr ${
                        applicationContext.getString(R.string.lower)
                    } $sellSet₽ (${currencyTCS.sell}₽)"
                    notification(notifyText)
                }
            }
        }
    }

    /**
     * Configure and show notification if actual values overrange user settings
     */
    private fun notification(textMessage: String) {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.notify)
            .setContentTitle(applicationContext.getString(R.string.attention))
            .setColorized(true)
            .setColor(Color.RED)
            .setContentText(textMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(textMessage)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val startAPP = Intent(applicationContext, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(applicationContext)
        stackBuilder.addNextIntentWithParentStack(startAPP)
        val pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pIntent)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }
}