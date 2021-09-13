package com.vadimko.curforeckotlin.forecastsMethods

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LessSquareTest {
    private lateinit var lessSquare: LessSquare
    private val forec = listOf(10.0F, 10.0F, 10.0F, 10.0F)

    @Before
    fun setUp() {
        val listToForec = mutableListOf(10.0F, 10.0F, 10.0F, 10.0F, 10.0F)
        lessSquare = LessSquare(listToForec)
        lessSquare.calc()
    }

    @Test
    fun calcForecastLessSquare() {
        assertEquals(lessSquare.getForecastVal(), forec)
    }

    @Test
    fun calcErrorLessSquare() {
        val error = "0"
        assertEquals(error, lessSquare.getErrVal())
    }


    @After
    fun tearDown() {
    }
}