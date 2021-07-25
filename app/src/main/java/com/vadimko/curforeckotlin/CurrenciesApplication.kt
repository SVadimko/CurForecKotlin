package com.vadimko.curforeckotlin

import android.app.Application
import android.content.Context
import com.vadimko.curforeckotlin.database.CurrenciesRepository

class CurrenciesApplication : Application() {
    override fun onCreate() {
        CurrenciesRepository.initialize(this)
        super.onCreate()
    }
}
