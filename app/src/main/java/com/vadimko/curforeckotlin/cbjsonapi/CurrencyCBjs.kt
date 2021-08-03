package com.vadimko.curforeckotlin.cbjsonapi

/**
 * data class used to store rates from the Central Bank
 */

data class CurrencyCBjs(
    var value: Double = 0.0,
    var valueWas: Double = 0.0,
    var dateTime: String = "",
    var flag: Int = 0,
    var curr: String = ""
)

