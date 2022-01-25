package com.vadimko.curforeckotlin.utils

import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbjsonApi.CBjsonResponse
import com.vadimko.curforeckotlin.cbjsonApi.CBjsonValute
import com.vadimko.curforeckotlin.cbjsonApi.CurrencyCBjs
import com.vadimko.curforeckotlin.cbxmlApi.CBXXMLResponse
import com.vadimko.curforeckotlin.cbxmlApi.CurrencyCBarhive
import com.vadimko.curforeckotlin.moexApi.CurrencyMOEX
import com.vadimko.curforeckotlin.moexApi.MOEXCandles
import com.vadimko.curforeckotlin.moexApi.MOEXResponse
import com.vadimko.curforeckotlin.tcsApi.*
import retrofit2.Response

/**
 * Util class for parsing response which get retrofit from servers
 */
object Parser {
    /**
     * Parsing response from Tinkov bank
     * @return list of [CurrencyTCS]
     */
    fun parseTcsResponse(
        response: Response<TCSResponse>
    ): List<CurrencyTCS> {
        val currentTCS: MutableList<CurrencyTCS> = mutableListOf()
        val tcsResponse: TCSResponse? = response.body()
        val tcsPayload: TCSPayload? = tcsResponse?.payload
        val tcsRates: List<TCSRates>? = tcsPayload?.rates
        val tcsLastUpdate: TCSLastUpdate? = tcsPayload?.lastUpdate
        val flagUSD = R.drawable.usd
        val nameUSD = tcsRates?.get(1)?.fromCurrency?.name
        val buyUSD = tcsRates?.get(1)?.buy
        val sellUSD = tcsRates?.get(1)?.sell
        val dt = tcsLastUpdate?.milliseconds
        val flagEUR = R.drawable.eur
        val nameEUR = tcsRates?.get(78)?.fromCurrency?.name
        val buyEUR = tcsRates?.get(78)?.buy
        val sellEUR = tcsRates?.get(78)?.sell
        val flagGBP = R.drawable.gbp
        val nameGBP = tcsRates?.get(27)?.fromCurrency?.name
        val buyGBP = tcsRates?.get(27)?.buy
        val sellGBP = tcsRates?.get(27)?.sell
        val usdTCS = CurrencyTCS(flagUSD, dt, sellUSD, buyUSD, nameUSD)
        val eurTCS = CurrencyTCS(flagEUR, dt, sellEUR, buyEUR, nameEUR)
        val gbpTCS = CurrencyTCS(flagGBP, dt, sellGBP, buyGBP, nameGBP)
        if (buyUSD != 0.0)
            currentTCS.add(usdTCS)
        if (buyEUR != 0.0)
            currentTCS.add(eurTCS)
        if (buyGBP != 0.0)
            currentTCS.add(gbpTCS)
        return currentTCS
    }

    /**
     * Parsing response from CB bank
     * @return list of [CurrencyCBjs]
     */
    fun parseCBResponse(response: Response<CBjsonResponse>): List<CurrencyCBjs> {
        val cBjsonResponse: CBjsonResponse? = response.body()
        val date: String? = cBjsonResponse?.date
        val dateSplit = date?.split("T")?.toTypedArray()
        val timeSplit = dateSplit?.get(1)?.split("+")?.toTypedArray()
        val dateWas = timeSplit!![0] + " " + dateSplit[0]
        val valuteResponse = cBjsonResponse.valute
        val valUSD: CBjsonValute = valuteResponse.usd
        val valEUR: CBjsonValute = valuteResponse.eur
        val valGBP: CBjsonValute = valuteResponse.gbp
        val valBYN: CBjsonValute = valuteResponse.byn
        val valTRY: CBjsonValute = valuteResponse.`try`
        val valUAH: CBjsonValute = valuteResponse.uah
        val valueUSD = valUSD.value
        val valueEUR = valEUR.value
        val valueGBP = valGBP.value
        val valueBYN = valBYN.value
        val valueTRY = valTRY.value
        val valueUAH = valUAH.value

        val previousUSD = valUSD.previous
        val previousEUR = valEUR.previous
        val previousGBP = valGBP.previous
        val previousBYN = valBYN.previous
        val previousTRY = valTRY.previous
        val previousUAH = valUAH.previous

        val flagUSD = R.drawable.usd
        val flagEUR = R.drawable.eur
        val flagGBP = R.drawable.gbp
        val flagBYN = R.drawable.byn
        val flagTRY = R.drawable.ty
        val flagUAH = R.drawable.uah

        val curUSD = CurrencyCBjs(valueUSD, previousUSD, dateWas, flagUSD, "USD")
        val curEUR = CurrencyCBjs(valueEUR, previousEUR, dateWas, flagEUR, "EUR")
        val curGBP = CurrencyCBjs(valueGBP, previousGBP, dateWas, flagGBP, "GBP")
        val curBYN = CurrencyCBjs(valueBYN, previousBYN, dateWas, flagBYN, "BYN")
        val curTRY = CurrencyCBjs(valueTRY / 10, previousTRY / 10, dateWas, flagTRY, "TRY")
        val curUAH = CurrencyCBjs(valueUAH / 10, previousUAH / 10, dateWas, flagUAH, "UAH")

        return listOf(curUSD, curEUR, curGBP, curBYN, curTRY, curUAH)
    }

    /**
     * Parsing response from MOEX
     * @return list of [CurrencyMOEX]
     */
    fun parseMoexResponse(response: Response<MOEXResponse>): List<CurrencyMOEX> {
        val moexResponse: MOEXResponse? = response.body()
        val moexCandles: MOEXCandles? = moexResponse?.candles
        //val moexcolumns = moexCandles?.columns
        val moexdata = moexCandles?.data
        val moexcurrency: MutableList<CurrencyMOEX> = mutableListOf()
        moexcurrency.clear()
        moexdata?.forEach { it ->
            val currencyMoex = CurrencyMOEX(
                it[6] as String,
                it[0] as Double,
                it[3] as Double,
                it[2] as Double,
                it[1] as Double,
                (it[3] as Double + it[2] as Double) / 2
            )
            moexcurrency.add(currencyMoex)
        }
        return moexcurrency
    }

    /**
     * Parsing response from CB bank
     * @return list of [CurrencyCBarhive]
     */
    fun parseCbXmlResponse(response: Response<CBXXMLResponse>): List<CurrencyCBarhive> {
        val listData: MutableList<CurrencyCBarhive> = mutableListOf()
        val cBXMLResponse = response.body()
        val listRec = cBXMLResponse?.record
        listRec?.forEach { it ->
            val offCur = it.Value
            val dateTime = it.Date
            val dateTimeConv = DateConverter.dateFormatForCbXML(dateTime)
            listData.add(CurrencyCBarhive(offCur, dateTime, dateTimeConv))
        }
        return listData
    }
}