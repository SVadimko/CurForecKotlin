package com.vadimko.curforeckotlin.cbjsonApi

import retrofit2.Call
import retrofit2.http.GET

/**
 * Interface for requesting Central Bank data with a response in JSON format
 */

interface CBjsonApi {

    @GET("daily_json.js")
    fun getCBForec(): Call<CBjsonResponse>

}