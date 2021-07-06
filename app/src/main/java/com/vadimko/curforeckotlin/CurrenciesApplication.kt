package com.vadimko.curforeckotlin

import android.app.Application
import com.vadimko.curforeckotlin.database.CurrenciesRepository

class CurrenciesApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        CurrenciesRepository.initialize(this)
    }
}