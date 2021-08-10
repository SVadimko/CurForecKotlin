package com.vadimko.curforeckotlin

import android.app.Application
import android.content.Context
import com.vadimko.curforeckotlin.database.CurrenciesRepository
import com.vadimko.curforeckotlin.prefs.NowPreference
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * creating application context singleton and init DataBase
 */

class CurrenciesApplication : Application() {
    init {
        instance = this
    }

    private val appModule = module {

        //single<HelloRepository> { HelloRepositoryImpl() }

        //factory { MySimplePresenter(get()) }

       // single { ClassToInject(androidContext()) }

        single {Saver}

        //single {NowPreference() }

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

    companion object {
        private var instance: CurrenciesApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}
