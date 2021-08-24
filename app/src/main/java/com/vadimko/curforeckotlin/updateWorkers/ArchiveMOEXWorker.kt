package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel

/**
 * Worker for calling [ArchiveViewModel.loadDataMOEX] request for MOEX data (not used)
 */

class ArchiveMOEXWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val request = inputData.getString("request")
        val from = inputData.getString("from")
        val till = inputData.getString("till")
        if (request != null) {
            if (from != null) {
                if (till != null) {
                    ArchiveViewModel.loadDataMOEX(request, from, till, "24")
                }
            }
        }
        return Result.success()
    }

}