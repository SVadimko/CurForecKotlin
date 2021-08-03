package com.vadimko.curforeckotlin

import android.app.Application
import android.content.Context
import com.vadimko.curforeckotlin.database.CurrenciesRepository

/**
 * creating application context singlton and init DataBase
 */

class CurrenciesApplication : Application() {
    init {
        instance = this
    }
    override fun onCreate() {
        CurrenciesRepository.initialize(this)
        super.onCreate()
    }

    companion object {
        private var instance: CurrenciesApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}
