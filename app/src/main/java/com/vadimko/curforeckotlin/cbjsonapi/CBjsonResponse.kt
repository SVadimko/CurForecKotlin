package com.vadimko.curforeckotlin.cbjsonapi

class CBjsonResponse {
    var Date: String = ""
    var Timestamp: String = ""
    lateinit var Valute: CBjsonValuteResponse
}

class CBjsonValuteResponse {
    lateinit var USD: CBjsonValute
    lateinit var EUR: CBjsonValute
    lateinit var GBP: CBjsonValute
    lateinit var BYN: CBjsonValute
    lateinit var TRY: CBjsonValute
    lateinit var UAH: CBjsonValute
}

class CBjsonValute {
    lateinit var ID: String
    lateinit var NumCode: String
    lateinit var CharCode: String
    var Nominal: Int = 1
    lateinit var Name: String
    var Value: Double = 0.0
    var Previous: Double = 0.0
}