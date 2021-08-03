package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.today.TodayViewModel

/**
 * worker for calling throw viewModel request for courses data for 1-5 days
 */

class TodayWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val request = inputData.getString("request")
        val from = inputData.getString("from")
        val till = inputData.getString("till")
        val interval = inputData.getString("interval")
        if (request != null) {
            if (from != null) {
                if (till != null) {
                    if (interval != null) {
                        TodayViewModel.loadDataMOEX(request,from,till,interval)
                    }
                }
            }
        }
        return Result.success()
    }

}