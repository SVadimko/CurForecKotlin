package com.vadimko.curforeckotlin.forecastsMethods

import java.text.NumberFormat

//рассчет прогноза метода меньших квадратов
class LessSquare(inp: MutableList<Float>) {
    var input: MutableList<Float> = inp
    var output: MutableList<Float> = mutableListOf()
    var forecast: MutableList<Float> = mutableListOf()
    val count = 0
    var input_summ = 0f
    var time_summ = 0f
    var input_summ_time = 0f
    var time_summ_square = 0f
    var alpha: Float = 0f
    var beta: Float = 0f
    var err_tmp = 0f
    var err: String = "0"

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
            input_summ = input_summ + input[i]
        }
    }

    private fun calculateTimeSumm() {
        for (i in 1 until input.size + 1) {
            time_summ = time_summ + i
        }
    }

    private fun calculateSummTime() {
        var y = 1
        for (i in input.indices) {
            input_summ_time = input_summ_time + input[i] * y
            y++
        }
    }

    private fun calculateTimeSummSquare() {
        for (i in 1 until input.size + 1) {
            time_summ_square = time_summ_square + (i * i)
        }
    }

    private fun calculateAlpha() {
        alpha =
            (input_summ_time - time_summ * input_summ / input.size) / (time_summ_square - time_summ * time_summ / input.size)
    }

    private fun calculateBeta() {
        beta = input_summ / input.size - alpha * time_summ / input.size
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

    //рассчитываем ошибку метода
    private fun calculateError() {
        for (i in input.indices) {
            err_tmp = err_tmp + Math.abs(input[i] - output[i]) / input[i] * 100
        }
    }

    fun getOutputVal(): MutableList<Float> {
        return output
    }

    //получаем прогноз
    fun getForecastVal(): MutableList<Float> {
        return forecast
    }

    fun getErrVal(): String {
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        err = numberFormat.format(err_tmp)
        return err
    }
}