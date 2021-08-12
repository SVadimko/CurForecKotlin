package com.vadimko.curforeckotlin.cbjsonApi

/**
 * Data class used to store rates from the Central Bank
 * @param value actual value of currency
 * @param valueWas past value of currency
 * @param dateTime time and date for actual value of currency
 * @param flag code that used to get access to the image of flag
 * @param curr name of currency
 */

data class CurrencyCBjs(
    var value: Double = 0.0,
    var valueWas: Double = 0.0,
    var dateTime: String = "",
    var flag: Int = 0,
    var curr: String = ""
)

