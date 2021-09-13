package com.vadimko.curforeckotlin.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.appModule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NowPreferenceTest : KoinTest {
    private val context: Context by inject()
    private val testNowPreferences: NowPreference by inject()
    private lateinit var sp: SharedPreferences

    @Before
    fun init() {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Test
    fun getTahoma() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }
        sp.edit().putString("font", "Tahoma").commit()
        val resultList = testNowPreferences.getTextParams()
        assertEquals(resultList[1], 23F)
    }

    @Test
    fun getSerif() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }
        sp.edit().putString("font", "NotoSerif").commit()
        val resultList = testNowPreferences.getTextParams()
        assertEquals(resultList[1], 24F)
    }

    @Test
    fun getDefValue() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }
        sp.edit().putString("font", "").commit()
        val resultList = testNowPreferences.getTextParams()
        assertEquals(resultList[1], 30F)
    }


}