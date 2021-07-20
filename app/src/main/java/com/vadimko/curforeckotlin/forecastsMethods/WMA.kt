package com.vadimko.curforeckotlin.forecastsMethods

import java.text.NumberFormat

class WMA(inp: MutableList<Float>, a: Int) {
    private var input: MutableList<Float> = inp
    private var output: MutableList<Float> = mutableListOf()
    private var forecast: MutableList<Float> = mutableListOf()
    private var averageError: Float? = null
    private var amount = a
    private var count = 0


    fun calc() {
        for (i in 1 until input.size - 1) {
            output.add((input[i - 1] + input[i] + input[i + 1]) / 3)
            count++
        }

        val inputLenght = input.size
        val outputLenght = output.size
        var inputTmp: Float
        forecast.add(input[input.size - 1])
        //формула вычисления прогноза (тут на три значения вперед)
        for (i in inputLenght until inputLenght + amount) {
            inputTmp =
                (input[i - 1] + input[i - 2] + input[i - 3]) / 3 + (input[i - 1] - input[i - 2]) / 3
            input.add(i, inputTmp)
            forecast.add(inputTmp)
        }
        //последние три значениея- прогноз

        //вычисление средней относительной ошибки прогноза
        var err = 0.0f
        for (i in 0 until outputLenght) {
            err = err + 100 * Math.abs(input[i] - output[i]) / input[i]
        }
        //средняя относительная ошибка прогноза
        averageError = err / outputLenght
    }

/*
    fun getWMA(): MutableList<Float> {
        return output
    }

    fun getInputWForecast(): MutableList<Float> {
        return input
    }
    */

    fun getForecast(): MutableList<Float> {
        return forecast
    }

    fun getAverageErr(): String? {
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 2
        return numberFormat.format(averageError)
    }

}