package com.vadimko.curforeckotlin

import android.app.Application
import android.content.Context
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel
import com.vadimko.curforeckotlin.ui.calc.CalcViewModel
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.ui.today.TodayViewModel
import com.vadimko.curforeckotlin.utils.Saver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Start KOIN, init DataBase and creating application context singleton(not used)
 * @property appModule KOIN module for inject dependencies
 */

class CurrenciesApplication : Application() {
    init {
        instance = this
    }

    private val appModule = module {

        single { Saver }

        viewModel { CalcViewModel(get()) }

        viewModel { NowViewModel(get()) }

        viewModel { TodayViewModel(get()) }

        viewModel { ArchiveViewModel(get()) }
    }


    override fun onCreate() {
        CurrenciesRepository.initialize(this)
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CurrenciesApplication)
            modules(appModule)
        }
    }

    /**
     * Singleton of app context (not used, replaced by Koin inject)
     */
    companion object {
        private var instance: CurrenciesApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}
