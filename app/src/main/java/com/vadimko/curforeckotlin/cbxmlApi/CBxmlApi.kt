package com.vadimko.curforeckotlin.cbxmlApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface for requesting Central Bank data with a response in XML format
 */

interface CBxmlApi {

    @GET("XML_dynamic.asp")
    fun getTCSForec(
        @Query("date_req1") date_req1: String,
        @Query("date_req2") date_req2: String,
        @Query("VAL_NM_RQ") VAL_NM_RQ: String
    ): Call<MOEXXMLResponse>
}