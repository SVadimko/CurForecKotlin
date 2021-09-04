package com.vadimko.curforeckotlin.tcsApi


/**
 * Splitting JSON response from Tinkov into classes
 */

class TCSResponse {
    lateinit var payload: TCSPayload
}

/**
 * Splitting JSON response from Tinkov into classes
 */
class TCSPayload {
    lateinit var rates: List<TCSRates>
    lateinit var lastUpdate: TCSLastUpdate
}

/**
 * Splitting JSON response from Tinkov into classes
 */
class TCSRates {
    lateinit var category: String
    lateinit var fromCurrency: FromCurrency

    //lateinit var toCurrency: toCurrency
    var buy = 0.0
    var sell = 0.0
}

/**
 * Splitting JSON response from Tinkov into classes
 */
class TCSLastUpdate {
    var milliseconds: Long = 0L
}

//data class toCurrency(val name:String = "")
/**
 * Splitting JSON response from Tinkov into classes
 */
data class FromCurrency(val name: String = "")