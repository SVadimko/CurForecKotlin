package com.vadimko.curforeckotlin.cbjsonApi

import com.google.gson.annotations.SerializedName

/**
 * Splitting JSON response from Central Bank into classes
 */
class CBjsonResponse {
    @SerializedName("Date")
    var date: String = ""

    //var Timestamp: String = ""
    @SerializedName("Valute")
    lateinit var valute: CBjsonValuteResponse
}

/**
 * Splitting JSON response from Central Bank into classes
 */
class CBjsonValuteResponse {
    @SerializedName("USD")
    lateinit var usd: CBjsonValute

    @SerializedName("EUR")
    lateinit var eur: CBjsonValute

    @SerializedName("GBP")
    lateinit var gbp: CBjsonValute

    @SerializedName("BYN")
    lateinit var byn: CBjsonValute

    @SerializedName("TRY")
    lateinit var `try`: CBjsonValute

    @SerializedName("UAH")
    lateinit var uah: CBjsonValute
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
    @SerializedName("Value")
    var value: Double = 0.0

    @SerializedName("Previous")
    var previous: Double = 0.0
}