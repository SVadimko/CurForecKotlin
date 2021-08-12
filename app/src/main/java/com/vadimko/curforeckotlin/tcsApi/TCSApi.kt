package com.vadimko.curforeckotlin.tcsApi

import retrofit2.Call
import retrofit2.http.GET

/**
 * Interface for requesting Tinkov data with a response in JSON format
 */

interface TCSApi {

    @GET("currency_rates/")
    fun getTCSForec(): Call<TCSResponse>

}