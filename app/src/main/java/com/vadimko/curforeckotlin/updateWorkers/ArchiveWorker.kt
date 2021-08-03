package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel

/**
 * worker for calling throw viewModel request for CB data
 */

class ArchiveWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val request = inputData.getString("request")
        val from = inputData.getString("from")
        val till = inputData.getString("till")
        //val interval = inputData.getString("interval")
        if (request != null) {
            if (from != null) {
                if (till != null) {
                    //Log.wtf("ArhiveWorker", "$request $from $till")
                    ArchiveViewModel.loadCBArhieve(from, till, request)
                }
            }
        }
        return Result.success()
    }

}