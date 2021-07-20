package com.vadimko.curforeckotlin.forecastsMethods

import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class LessSquareTest {
    lateinit var lessSquare: LessSquare
    val forec = listOf<Float>(10.0F, 10.0F, 10.0F, 10.0F)

    @Before
    fun setUp() {
        val listToForec = mutableListOf<Float>(10.0F, 10.0F, 10.0F, 10.0F, 10.0F)
        lessSquare = LessSquare(listToForec)
        lessSquare.calc()
    }

    @Test
    fun calcForecastLessSquare() {
        assertEquals(lessSquare.getForecastVal(), forec)
    }

    @Test
    fun calcErrorLessSquare(){
        val error = "0"
        assertEquals(error, lessSquare.getErrVal())
    }


    @After
    fun tearDown() {
    }
}