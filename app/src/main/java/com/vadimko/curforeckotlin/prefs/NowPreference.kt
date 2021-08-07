package com.vadimko.curforeckotlin.prefs

import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.CurrenciesApplication

object NowPreference {
    fun getTexParams(): List<Any> {
        val typefaceTv: Typeface
        val textSizeFlt: Float
        val fontSelector =
            PreferenceManager.getDefaultSharedPreferences(CurrenciesApplication.applicationContext())
                .getString("font", "")
        when (fontSelector) {
            "Tahoma" -> {
                typefaceTv = ResourcesCompat.getFont(
                    CurrenciesApplication.applicationContext(),
                    com.vadimko.curforeckotlin.R.font.tahoma
                )!!
                textSizeFlt = 23F
            }
            "NotoSerif" -> {
                typefaceTv = ResourcesCompat.getFont(
                    CurrenciesApplication.applicationContext(),
                    com.vadimko.curforeckotlin.R.font.notoserif
                )!!
                textSizeFlt = 23F
            }
            else -> {
                typefaceTv = ResourcesCompat.getFont(
                    CurrenciesApplication.applicationContext(),
                    com.vadimko.curforeckotlin.R.font.digital
                )!!
                textSizeFlt = 30F
            }
        }
        return listOf(typefaceTv, textSizeFlt)
    }
}