package com.vadimko.curforeckotlin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.vadimko.curforeckotlin.utils.DateConverter.androidHigherN
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Util class for converting dates with patterns that accept servers
 * @property androidHigherN flag shows is SDK > N ot not
 */

object DateConverter : KoinComponent {
    private val context: Context by inject()
    private val androidHigherN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    /**
     * Used to convert dates received from DataPickerFragments to ArchiveFragment
     */
    fun getFromTillDate(
        fromDate: Date,
        tillDate: Date,
    ): ArrayList<Array<String>> {
        val result = ArrayList<Array<String>>()
        val till: Date = tillDate
        val from: Date = fromDate
        val jdf = if (androidHigherN) {
            SimpleDateFormat("yyyy-MM-dd", context.resources.configuration.locales[0])
        } else {
            SimpleDateFormat("yyyy-MM-dd", context.resources.configuration.locale)
        }
        val res = arrayOf(jdf.format(from), jdf.format(till))
        result.add(0, res)
        val jdf2 = if (androidHigherN) {
            SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locales[0])
        } else {
            SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locale)
        }
        val res2 = arrayOf(jdf2.format(from), jdf2.format(till))
        result.add(1, res2)
        return result
    }

    /**
     * Used to convert dates received from DataPickerFragments and set them to UI in ArhiveFragment
     */
    fun dateWithOutTimeFormat(date: Date): String {
        val jdf = if (androidHigherN) {
            SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locales[0])
        } else {
            SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locale)
        }
        return jdf.format(date)
    }

    /**
     * Used to convert dates received from Tinkov to represent them in UI and charts
     */
    fun longToDateWithTime(time: Long): String {
        return if (androidHigherN) {
            SimpleDateFormat(
                "HH:mm:ss dd.MM.yyyy",
                context.resources.configuration.locales[0]
            ).format(
                Date(
                    time
                )
            )
        } else {
            return SimpleDateFormat(
                "HH:mm:ss dd.MM.yyyy",
                context.resources.configuration.locale
            ).format(
                Date(time)
            )
        }
    }

    /**
     * Used to format data for response from CB in xml
     */
    @SuppressLint("SimpleDateFormat")
    fun dateFormatForCbXML(datesOFF: String): String {
        val dateConvert = SimpleDateFormat("dd.MM.yyyy").parse(datesOFF)
        val jdf = SimpleDateFormat("yyyy-MM-dd")
        return jdf.format(dateConvert!!)
    }
}
