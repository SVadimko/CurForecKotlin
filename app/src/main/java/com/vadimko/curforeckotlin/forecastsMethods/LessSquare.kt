package com.vadimko.curforeckotlin.forecastsMethods

import java.text.NumberFormat
import kotlin.math.abs

/**
 *  Calculate forecast using method of Less Square
 */

class LessSquare(inp: MutableList<Float>) {
    private var input: MutableList<Float> = inp
    private var output: MutableList<Float> = mutableListOf()
    private var forecast: MutableList<Float> = mutableListOf()

    //val count = 0
    private var inputSumm = 0f
    private var timeSumm = 0f
    private var inputSummTime = 0f
    private var timeSummSquare = 0f
    private var alpha: Float = 0f
    private var beta: Float = 0f
    private var errTmp = 0f
    private var err: String = "0"

    fun calc() {
        calculateInputSumm()
        calculateTimeSumm()
        calculateSummTime()
        calculateTimeSummSquare()
        calculateAlpha()
        calculateBeta()
        calculateOutput()
        calculateForecast()
        calculateError()
    }

    private fun calculateInputSumm() {
        for (i in input.indices) {
            inputSumm += input[i]
        }
    }

    private fun calculateTimeSumm() {
        for (i in 1 until input.size + 1) {
            timeSumm += i
        }
    }

    private fun calculateSummTime() {
        var y = 1
        for (i in input.indices) {
            inputSummTime += input[i] * y
            y++
        }
    }

    private fun calculateTimeSummSquare() {
        for (i in 1 until input.size + 1) {
            timeSummSquare += (i * i)
        }
    }

    private fun calculateAlpha() {
        alpha =
            (inputSummTime - timeSumm * inputSumm / input.size) / (timeSummSquare - timeSumm * timeSumm / input.size)
    }

    private fun calculateBeta() {
        beta = inputSumm / input.size - alpha * timeSumm / input.size
    }

    private fun calculateOutput() {
        output.clear()
        for (i in 1 until input.size + 1) {
            output.add(alpha * i + beta)
        }
    }

    private fun calculateForecast() {
        forecast.clear()
        for (i in input.size until input.size + 4) {
            forecast.add(alpha * i.toFloat() + beta)
        }
    }

    /**
     * Calculating methods error
     */
    private fun calculateError() {
        for (i in input.indices) {
            errTmp += abs(input[i] - output[i]) / input[i] * 100
        }
    }

    fun getOutputVal(): MutableList<Float> {
        return output
    }

    /**
     * @return forecast value
     */
    fun getForecastVal(): MutableList<Float> {
        return forecast
    }

    /**
     * @return error of calculating method
     */
    fun getErrVal(): String {
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        err = numberFormat.format(errTmp)
        return err
    }
}