package com.vadimko.curforeckotlin.forecastsMethods

import java.text.NumberFormat
import kotlin.math.abs

//прогноз методом экспоненциального взвешивания
class ExponentSmooth(inp: MutableList<Float>) {
    private var input: MutableList<Float> = inp
    private var output: MutableList<Float> = mutableListOf()
    private var output2: MutableList<Float> = mutableListOf()
    private var forecast: MutableList<Float> = mutableListOf()
    private var forecast2: MutableList<Float> = mutableListOf()
    private var alpha = 0f
    private var uo1 = 0f
    private var uo2 = 0f
    private var err1: String? = null
    private var err2: String? = null


    fun calc() {
        calculateAlpha()
        calculateUo1()
        calculateUo2()
        calculateSmooth1()
        calculateSmooth2()
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        err1 = numberFormat.format(calculateErr(input, output).toDouble())
        err2 = numberFormat.format(calculateErr(input, output2).toDouble())
    }


    private fun calculateAlpha() {
        alpha = 2 / (input.size.toFloat() + 1)
    }

    private fun calculateUo1() {
        var summ = 0f
        for (i in input.indices) summ += input[i]
        uo1 = summ / input.size
    }

    private fun calculateUo2() {
        uo2 = input[0]
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

    private fun calculateSmooth2() {
        output2.clear()
        output2.add(0, uo2)
        for (i in 1 until input.size + 1) {
            output2.add(i, input[i - 1] * alpha + (1 - alpha) * output2[i - 1])
        }
        forecast2.add(output2[output2.size - 2])
        forecast2.add(output2[output2.size - 1])
    }

    //вычисление ошибки
    private fun calculateErr(inp: MutableList<Float>, out: MutableList<Float>): Float {
        var err = 0f
        for (i in inp.indices) {
            err += abs(inp[i] - out[i]) / inp[i] * 100
        }
        err /= inp.size
        return err
    }

    fun getSmooth1(): MutableList<Float> {
        val res = output
        res.removeAt(res.size - 1)
        return res
    }

    //получаем прогноз
    fun getForecast1(): MutableList<Float> {
        return forecast
    }

    fun getErrSmppth1(): String? {
        return err1
    }

    /*fun getSmooth2(): MutableList<Float> {
        val res = output2
        res.removeAt(res.size - 1)
        return res
    }
    */
}