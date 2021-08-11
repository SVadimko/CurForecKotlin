package com.vadimko.curforeckotlin.tcsApi

import android.annotation.SuppressLint
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * data class used to store rates from the Tinkov bank
 */

data class CurrencyTCS(
    var flag: Int,
    var datetime: Long?,
    var sell: Double?,
    var buy: Double?,
    var name: String?
) : Serializable {


    var curr = longToTime(datetime)

    @SuppressLint("SimpleDateFormat")
    fun longToTime(dt: Long?): String? {
        return if (datetime!! > 0) {
            val date = dt?.let { Date(it) }
            val jdf = SimpleDateFormat("HH:mm:ss yyyy-MM-dd")
            jdf.format(date!!)
        } else ""
    }

    override fun toString(): String {
        return "$sell $buy"
    }
}
