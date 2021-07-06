package com.vadimko.curforeckotlin.cbjsonapi

import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CBjsonRepository {
    private val cBjsonApi: CBjsonApi

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        cBjsonApi = retrofit.create(CBjsonApi::class.java)
    }

    fun getCurrentCB() {
        val currentRequest: Call<CBjsonResponse> = cBjsonApi.getCBForec()
        currentRequest.enqueue(object : Callback<CBjsonResponse> {
            override fun onResponse(
                call: Call<CBjsonResponse>,
                response: Response<CBjsonResponse>
            ) {
                val cBjsonResponse: CBjsonResponse? = response.body()
                val date: String? = cBjsonResponse?.Date
                val dateSplit = date?.split("T")?.toTypedArray()
                val timeSplit = dateSplit?.get(1)?.split("+")?.toTypedArray()
                val dateWas = timeSplit!![0] + " " + dateSplit[0]
                val valuteResponse = cBjsonResponse.Valute
                val USD: CBjsonValute = valuteResponse.USD
                val EUR: CBjsonValute = valuteResponse.EUR
                val GBP: CBjsonValute = valuteResponse.GBP
                val BYN: CBjsonValute = valuteResponse.BYN
                val TRY: CBjsonValute = valuteResponse.TRY
                val UAH: CBjsonValute = valuteResponse.UAH
                val valueUSD = USD.Value
                val valueEUR = EUR.Value
                val valueGBP = GBP.Value
                val valueBYN = BYN.Value
                val valueTRY = TRY.Value
                val valueUAH = UAH.Value

                val previousUSD = USD.Previous
                val previousEUR = EUR.Previous
                val previousGBP = GBP.Previous
                val previousBYN = BYN.Previous
                val previousTRY = TRY.Previous
                val previousUAH = UAH.Previous

                val flagUSD = R.drawable.usd
                val flagEUR = R.drawable.eur
                val flagGBP = R.drawable.gbp
                val flagBYN = R.drawable.byn
                val flagTRY = R.drawable.ty
                val flagUAH = R.drawable.uah

                val curUSD = CurrencyCBjs(valueUSD, previousUSD, dateWas, flagUSD, "USD")
                val curEUR = CurrencyCBjs(valueEUR, previousEUR, dateWas, flagEUR, "EUR")
                val curGBP = CurrencyCBjs(valueGBP, previousGBP, dateWas, flagGBP, "GBP")
                val curBYN = CurrencyCBjs(valueBYN, previousBYN, dateWas, flagBYN, "UAH")
                val curTRY = CurrencyCBjs(valueTRY / 10, previousTRY / 10, dateWas, flagTRY, "TRY")
                val curUAH = CurrencyCBjs(valueUAH / 10, previousUAH / 10, dateWas, flagUAH, "BYN")

                val cbCurr: List<CurrencyCBjs> =
                    listOf(curUSD, curEUR, curGBP, curBYN, curTRY, curUAH)
                NowViewModel.dataCB.postValue(cbCurr)
            }

            override fun onFailure(call: Call<CBjsonResponse>, t: Throwable) {
            }
        })
    }
}