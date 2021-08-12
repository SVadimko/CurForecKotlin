package com.vadimko.curforeckotlin.moexApi

/**
 * Data class used to store rates from the MOEX
 * @param dates date and time of currency values
 * @param open value at opening
 * @param low low value at this time
 * @param high high value at this time
 * @param close value at closing
 * @param warprice weighted average value
 */

data class CurrencyMOEX(
    val dates: String = "",
    val open: Double = 0.0,
    val low: Double = 0.0,
    val high: Double = 0.0,
    val close: Double = 0.0,
    val warprice: Double = 0.0,
)