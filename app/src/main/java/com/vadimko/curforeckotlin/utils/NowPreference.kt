package com.vadimko.curforeckotlin.utils

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.utils.NowPreference.context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Util class to load font prefs
 * @property context Application context injected by Koin
 */
object NowPreference : KoinComponent {

    private val context: Context by inject()

    /**
     * @return typefaceTv and textsize
     */
    fun getTextParams(): List<Any> {
        val typefaceTv: Typeface
        val textSizeFlt: Float
        val fontSelector =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString("font", "")
        when (fontSelector) {
            "Tahoma" -> {
                typefaceTv = ResourcesCompat.getFont(
                    context,
                    com.vadimko.curforeckotlin.R.font.tahoma
                )!!
                textSizeFlt = 23F
            }
            "NotoSerif" -> {
                typefaceTv = ResourcesCompat.getFont(
                    context,
                    com.vadimko.curforeckotlin.R.font.notoserif
                )!!
                textSizeFlt = 24F
            }
            else -> {
                typefaceTv = ResourcesCompat.getFont(
                    context,
                    com.vadimko.curforeckotlin.R.font.digital
                )!!
                textSizeFlt = 30F
            }
        }
        return listOf(typefaceTv, textSizeFlt)
    }
}