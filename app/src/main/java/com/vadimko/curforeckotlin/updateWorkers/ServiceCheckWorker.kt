package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.TCSUpdateService
import com.vadimko.curforeckotlin.utils.CheckTCSUpdateServiceIsRunning

/**
 * Utility worker class, that in case of auto update enabled in SharedPreference runs periodically
 * and check via [CheckTCSUpdateServiceIsRunning.checkAutoUpdate] if [TCSUpdateService] running or not and launch it,
 * if it was killed
 */
class ServiceCheckWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        CheckTCSUpdateServiceIsRunning.checkAutoUpdate()
        return Result.success()
    }
}