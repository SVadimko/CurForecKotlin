package com.vadimko.curforeckotlin.cbjsonApi

/**
 * Splitting JSON response from Central Bank into classes
 */
class CBjsonResponse {
    var Date: String = ""

    //var Timestamp: String = ""
    lateinit var Valute: CBjsonValuteResponse
}

/**
 * Splitting JSON response from Central Bank into classes
 */
class CBjsonValuteResponse {
    lateinit var USD: CBjsonValute
    lateinit var EUR: CBjsonValute
    lateinit var GBP: CBjsonValute
    lateinit var BYN: CBjsonValute
    lateinit var TRY: CBjsonValute
    lateinit var UAH: CBjsonValute
}

/**
 * Splitting JSON response from Central Bank into classes
 */
class CBjsonValute {
    //lateinit var ID: String
    //lateinit var NumCode: String
    //lateinit var CharCode: String
    //var Nominal: Int = 1
    //lateinit var Name: String
    var Value: Double = 0.0
    var Previous: Double = 0.0
}