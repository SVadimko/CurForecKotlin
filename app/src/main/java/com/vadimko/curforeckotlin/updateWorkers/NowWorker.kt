package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.now.NowViewModel

/**
 * Worker for calling throw viewModel request for Tinkov and CB data (not used,
 * replaced by coroutines in [NowViewModel])
 */

class NowWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        NowViewModel.loadDataTCS()
        NowViewModel.loadDataCB()
        return Result.success()
    }
}