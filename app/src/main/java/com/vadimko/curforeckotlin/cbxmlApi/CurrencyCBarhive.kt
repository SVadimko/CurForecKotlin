package com.vadimko.curforeckotlin.cbxmlApi

/**
 * data class used to store archive rates from the Central Bank
 */
data class CurrencyCBarhive(
    var offCur: String = "",
    var dateTime: String = "",
    var datetimeConv: String = ""
)