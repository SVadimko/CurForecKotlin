package com.vadimko.curforeckotlin.forecastsMethods

import java.text.NumberFormat

/**
 * Calculate forecat using weighted moving average method (WMA)
 */
class WMA(inp: MutableList<Float>, a: Int) {
    private var input: MutableList<Float> = inp
    private var output: MutableList<Float> = mutableListOf()
    private var forecast: MutableList<Float> = mutableListOf()
    private var averageError: Float? = null
    private var amount = a
    private var count = 0

    /**
     * Calculate forecast (3 values ahead) [forecast]
     * Calculate error of method [averageError]
     */
    fun calc() {
        for (i in 1 until input.size - 1) {
            output.add((input[i - 1] + input[i] + input[i + 1]) / 3)
            count++
        }
        val inputLenght = input.size
        val outputLenght = output.size
        var inputTmp: Float
        forecast.add(input[input.size - 1])
        for (i in inputLenght until inputLenght + amount) {
            inputTmp =
                (input[i - 1] + input[i - 2] + input[i - 3]) / 3 + (input[i - 1] - input[i - 2]) / 3
            input.add(i, inputTmp)
            forecast.add(inputTmp)
        }
        var err = 0.0f
        for (i in 0 until outputLenght) {
            err = err + 100 * Math.abs(input[i] - output[i]) / input[i]
        }
        averageError = err / outputLenght
    }


    /**
     * @return calculated forecast
     */
    fun getForecast(): MutableList<Float> {
        return forecast
    }

    /**
     * @return formatted error value of calculating
     */
    fun getAverageErr(): String? {
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        return numberFormat.format(averageError)
    }

}