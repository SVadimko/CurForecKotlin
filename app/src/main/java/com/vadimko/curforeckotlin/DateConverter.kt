package com.vadimko.curforeckotlin

import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*

/**
 * util class for converting dates with patterns that accept servers
 */

class DateConverter {
    companion object {
        fun getFromTillDate(
            fromDate: Date,
            tillDate: Date,
            context: Context,
        ): ArrayList<Array<String>> {
            val result = ArrayList<Array<String>>()
            val till: Date = tillDate
            val from: Date = fromDate
            val jdf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("yyyy-MM-dd", context.resources.configuration.locales[0])
            } else {
                SimpleDateFormat("yyyy-MM-dd", context.resources.configuration.locale)
            }
            val res = arrayOf(jdf.format(from), jdf.format(till))
            result.add(0, res)
            val jdf2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locales[0])
            } else {
                SimpleDateFormat("dd/MM/yyyy", context.resources.configuration.locale)
            }
            val res2 = arrayOf(jdf2.format(from), jdf2.format(till))
            result.add(1, res2)
            return result
        }
    }
}
