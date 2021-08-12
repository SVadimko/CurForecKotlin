package com.vadimko.curforeckotlin.forecastsMethods

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExponentSmoothTest {
    lateinit var exponentSmooth: ExponentSmooth
    val forec = listOf<Float>(10.0F, 10.0F)

    @Before
    fun setUp() {
        val listToForec = mutableListOf<Float>(10.0F, 10.0F, 10.0F, 10.0F, 10.0F)
        exponentSmooth = ExponentSmooth(listToForec)
        exponentSmooth.calc()

    }

    @Test
    fun calcExponentSmoothTest() {
        assertEquals(exponentSmooth.getForecast(), forec)
    }

    @Test
    fun calcErrorSmoothTest() {
        val error = "0"
        assertEquals(exponentSmooth.getErrSmooth(), error)
    }

    @After
    fun tearDown() {
    }
}