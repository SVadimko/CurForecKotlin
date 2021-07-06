package com.vadimko.curforeckotlin.cbjsonapi

data class CurrencyCBjs(
    var value: Double = 0.0,
    var value_was: Double = 0.0,
    var datetime: String = "",
    var flag: Int = 0,
    var curr: String = ""
)

