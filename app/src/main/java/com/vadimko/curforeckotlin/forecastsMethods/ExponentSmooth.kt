package com.vadimko.curforeckotlin.forecastsMethods

import java.text.NumberFormat
import kotlin.math.abs

/**
 * Forecast using exponential weighting
 */

class ExponentSmooth(inp: MutableList<Float>) {
    private var input: MutableList<Float> = inp
    private var output: MutableList<Float> = mutableListOf()
    private var forecast: MutableList<Float> = mutableListOf()
    private var alpha = 0f
    private var uo1 = 0f
    private var err1: String? = null


    fun calc() {
        calculateAlpha()
        calculateUo1()
        calculateSmooth1()
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        err1 = numberFormat.format(calculateErr(input, output).toDouble())
    }


    private fun calculateAlpha() {
        alpha = 2 / (input.size.toFloat() + 1)
    }

    private fun calculateUo1() {
        var summ = 0f
        for (i in input.indices) summ += input[i]
        uo1 = summ / input.size
    }

    private fun calculateSmooth1() {
        output.clear()
        output.add(0, uo1)
        for (i in 1 until input.size + 1) {
            output.add(i, input[i - 1] * alpha + (1 - alpha) * output[i - 1])
        }
        forecast.add(output[output.size - 2])
        forecast.add(output[output.size - 1])
    }

    /**
     * Calculating error in percent
     */
    private fun calculateErr(inp: MutableList<Float>, out: MutableList<Float>): Float {
        var err = 0f
        for (i in inp.indices) {
            err += abs(inp[i] - out[i]) / inp[i] * 100
        }
        err /= inp.size
        return err
    }

    /**
     * @return data for smooth graph value
     */
    fun getSmooth(): MutableList<Float> {
        val res = output
        res.removeAt(res.size - 1)
        return res
    }

    /**
     * @return forecast
     */
    fun getForecast(): MutableList<Float> {
        return forecast
    }

    /**
     * @return error of calculating
     */
    fun getErrSmooth(): String? {
        return err1
    }

}