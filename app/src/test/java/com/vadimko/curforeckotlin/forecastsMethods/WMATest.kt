package com.vadimko.curforeckotlin.forecastsMethods

import android.util.Log
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.text.NumberFormat

class WMATest {
    lateinit var wmaTest: WMA
    val forec = listOf<Float>(10.0F, 10.0F, 10.0F, 10.0F)
    val lenght = forec.size - 1

    @Before
    fun setUp() {
        val listToForec = mutableListOf<Float>(10.0F, 10.0F, 10.0F, 10.0F, 10.0F)
        wmaTest = WMA(listToForec, lenght)
        wmaTest.calc()
    }

    @Test
    fun calcForecastWMA() {
        assertEquals(forec, wmaTest.getForecast())
    }

    @Test
    fun calcErrorWMA() {
        val error = "0"
        assertEquals(wmaTest.getAverageErr(), error)
    }

    @After
    fun tearDown() {
    }
}