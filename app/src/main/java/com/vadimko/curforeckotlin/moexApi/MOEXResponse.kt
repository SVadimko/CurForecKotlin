package com.vadimko.curforeckotlin.moexApi


/**
 * Splitting response from MOEX into classes
 */

class MOEXResponse {
    lateinit var candles: MOEXCandles
}

/**
 * Splitting response from MOEX into classes
 */
class MOEXCandles {
    lateinit var data: List<List<Any>>
    //lateinit var columns: List<String>
}