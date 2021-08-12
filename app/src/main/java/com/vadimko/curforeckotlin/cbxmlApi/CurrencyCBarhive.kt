package com.vadimko.curforeckotlin.cbxmlApi

/**
 * Data class used to store archive rates from the Central Bank
 * @param offCur official value of currency
 * @param dateTime date and time provided by server
 * @param datetimeConv converted date and time
 */
data class CurrencyCBarhive(
    var offCur: String = "",
    var dateTime: String = "",
    var datetimeConv: String = ""
)