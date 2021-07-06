package com.vadimko.curforeckotlin.tcsapi

class TCSResponse {
    lateinit var payload: TCSPayload
}

class TCSPayload {
    lateinit var rates: List<TCSRates>
    lateinit var lastUpdate: TCSLastUpdate
}

class TCSRates {
    lateinit var category: String
    lateinit var fromCurrency: fromCurrency
    lateinit var toCurrency: toCurrency
    var buy =0.0
    var sell =0.0
}

class TCSLastUpdate {
    var milliseconds: Long = 0L
}

data class toCurrency(val name:String = "")

data class fromCurrency (val name:String = "")