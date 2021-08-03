package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.now.NowViewModel

/**
 * worker for calling throw viewModel request for Tinkov and CB data
 */

class NowWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        NowViewModel.loadDataTCS()
        NowViewModel.loadDataCB()
        return Result.success()
    }
}