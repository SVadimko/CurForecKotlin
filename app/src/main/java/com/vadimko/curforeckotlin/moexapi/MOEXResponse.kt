package com.vadimko.curforeckotlin.moexapi


/**
 * splitting response into classes
 */

class MOEXResponse {
    lateinit var candles: MOEXCandles
}


class MOEXCandles {
    lateinit var data: List<List<Any>>
    //lateinit var columns: List<String>
}