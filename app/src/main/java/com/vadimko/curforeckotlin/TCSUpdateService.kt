package com.vadimko.curforeckotlin

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.tcsapi.*
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "11"
var NOTIFICATION_ID = 5555
private const val notificationId = 11

/**
 * service for auto-updating the rates of the Tinkov bank
 */

class TCSUpdateService : Service() {
    private val tcsApi: TCSApi

    private var period: Long = 5

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.tinkoff.ru/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        tcsApi = retrofit.create(TCSApi::class.java)

    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (checkConnection()) updateTask()
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
        //period = PreferenceManager.getDefaultSharedPreferences(this)
            //.getString("update_per", "15_min")?.split("_")?.get(0)?.toLong()!!
        Thread {
            var allRequest = 1
            while (true) {
                period = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("update_per", "15_min")?.split("_")?.get(0)?.toLong()!!
                if (checkConnection()) getCurrentTCS()
                for (y in 0 until period * 12) {
                    try {
                        TimeUnit.SECONDS.sleep(5)
                        if (!PreferenceManager.getDefaultSharedPreferences(this)
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
                if (!PreferenceManager.getDefaultSharedPreferences(this)
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
        }.start()
    }

    fun getCurrentTCS() {
        var currentTCS: MutableList<CurrencyTCS>
        try {
            val currentRequest: Call<TCSResponse> = tcsApi.getTCSForec()
            currentRequest.enqueue(object : Callback<TCSResponse> {
                override fun onResponse(call: Call<TCSResponse>, response: Response<TCSResponse>) {
                    val tcsResponse: TCSResponse? = response.body()
                    val tcsPayload: TCSPayload? = tcsResponse?.payload
                    val tcsRates: List<TCSRates>? = tcsPayload?.rates
                    val tcslastUpdate: TCSLastUpdate? = tcsPayload?.lastUpdate
                    val flagUSD = R.drawable.usd
                    val nameUSD = tcsRates?.get(15)?.fromCurrency?.name
                    val buyUSD = tcsRates?.get(15)?.buy
                    val sellUSD = tcsRates?.get(15)?.sell
                    val dt = tcslastUpdate?.milliseconds
                    val flagEUR = R.drawable.eur
                    val nameEUR = tcsRates?.get(18)?.fromCurrency?.name
                    val buyEUR = tcsRates?.get(18)?.buy
                    val sellEUR = tcsRates?.get(18)?.sell
                    val flagGBP = R.drawable.gbp
                    val nameGBP = tcsRates?.get(21)?.fromCurrency?.name
                    val buyGBP = tcsRates?.get(21)?.buy
                    val sellGBP = tcsRates?.get(21)?.sell
                    val usdTCS = CurrencyTCS(flagUSD, dt, sellUSD, buyUSD, nameUSD)
                    val eurTCS = CurrencyTCS(flagEUR, dt, sellEUR, buyEUR, nameEUR)
                    val gbpTCS = CurrencyTCS(flagGBP, dt, sellGBP, buyGBP, nameGBP)

                    if (usdTCS.buy == 0.0 || eurTCS.buy == 0.0 || gbpTCS.buy == 0.0) {
                        getCurrentTCS()
                    } else {
                        currentTCS = mutableListOf(usdTCS, eurTCS, gbpTCS)
                        GlobalScope.launch(Dispatchers.IO) {
                            Saver().saveTcsLast(currentTCS)
                            CalcViewModel.data2.postValue(Saver().loadTcsLast())
                        }
                        val usdBuy = String.format("%.2f", currentTCS[0].buy)
                        val usdSell = String.format("%.2f", currentTCS[0].sell)
                        val eurBuy = String.format("%.2f", currentTCS[1].buy)
                        val eurSell = String.format("%.2f", currentTCS[1].sell)
                        val gbpBuy = String.format("%.2f", currentTCS[2].buy)
                        val gbpSell = String.format("%.2f", currentTCS[2].sell)
                        startForeground(
                            NOTIFICATION_ID,
                            buildForegroundNotification(
                                "USD: $usdBuy - $usdSell",
                                "EUR $eurBuy $eurSell | GBP $gbpBuy $gbpSell"
                            )
                        )
                        checkCurrencyLevel(currentTCS)
                    }
                }

                override fun onFailure(call: Call<TCSResponse>, t: Throwable) {
                }
            })
        } catch (th: Throwable) {
            th.printStackTrace()
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

    private fun checkConnection(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        return caps?.hasCapability(NET_CAPABILITY_INTERNET) ?: false
    }
}