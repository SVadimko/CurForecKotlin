package com.vadimko.curforeckotlin.moexapi


data class CurrencyMOEX(
    val dates: String = "",
    val open: Double = 0.0,
    val low: Double = 0.0,
    val high: Double = 0.0,
    val close: Double = 0.0,
    val warprice: Double = 0.0,
)