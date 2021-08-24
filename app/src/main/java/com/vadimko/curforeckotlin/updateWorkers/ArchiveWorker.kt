package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.archive.ArchiveViewModel

/**
 * Worker for calling [ArchiveViewModel.loadCBArchive] request for CB data (not used)
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
                    ArchiveViewModel.loadCBArchive(from, till, request)
                }
            }
        }
        return Result.success()
    }

}