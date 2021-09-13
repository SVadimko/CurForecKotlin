package com.vadimko.curforeckotlin.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.appModule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class TodayPreferencesTest : KoinTest {
    private val context: Context by inject()
    private val testTodayPreferences: TodayPreferences by inject()


    /* @get:Rule
     val koinTestRule = KoinTestRule.create {
         printLogger()
         modules(appModule)
     }*/

    /* @Before
     fun setUp() {
       startKoin(KoinTestRule.create {
           printLogger()
           modules(appModule)
       })
     }*/


    /*  @After
      fun tearDown() {
          stopKoin()
      }*/


    @Test
    fun loadDefaultPrefs() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }

        val till = Date(System.currentTimeMillis())
        val from = Date(System.currentTimeMillis() - 86400000 * 1)
        val result = DateConverter.getFromTillDate(from, till)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit().clear().commit()
        val defaultList = listOf("USD000000TOD", result[0][0], result[0][1], "10", "0", "0", "0")
        assertEquals(defaultList, testTodayPreferences.loadPrefs())

    }

    @Test
    fun saveAndLoadTodayPrefs() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }
        // val testTodayPreferences: TodayPreferences by inject()
        val list = listOf("str1", "str2", "str3", "str4", "0", "1", "2")
        testTodayPreferences.savePrefs("str1", "str2", "str3", "str4", 0, 1, 2)
        assertEquals(list, testTodayPreferences.loadPrefs())

    }
}