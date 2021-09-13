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
class ArchivePreferencesTest : KoinTest {
    private val context: Context by inject()
    private val testArchivePreferences: ArchivePreferences by inject()

    /* @get:Rule
     val koinTestRule = KoinTestRule.create {
         printLogger()
         modules(appModule)
     }*/

    /*  @Before
      fun setUp() {
          KoinTestRule.create {
              printLogger()
              modules(appModule)
          }
      }*/


    /* @After
     fun tearDown() {
         stopKoin()
         Log.wtf("Stop", "Stop")
     }
 */
    @Test
    fun loadDefaultPrefs() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }
        val from = Date(System.currentTimeMillis() - 604800000)
        val till = Date(System.currentTimeMillis())
        val result = DateConverter.getFromTillDate(from, till)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit().clear().commit()
        val list = testArchivePreferences.loadPrefs()
        assertEquals(
            list, listOf(
                list[0],
                list[1], "0", "R01235", result[1][0], result[1][1],
                "USD000000TOD", result[0][0], result[0][1], "24", "0"
            )
        )
    }

    @Test
    fun saveAndLoadArchivePrefs() {
        KoinTestRule.create {
            printLogger()
            modules(appModule)
        }
        val list = listOf(
            "0", "1", "2", "str1", "str2",
            "str3", "str4", "str5", "str6", "str7", "0"
        )
        testArchivePreferences.savePrefs(
            0, 1, 2, "str1", "str2",
            "str3", "str4", "str5", "str6", "str7", 0
        )
        assertEquals(list, testArchivePreferences.loadPrefs())
    }
}