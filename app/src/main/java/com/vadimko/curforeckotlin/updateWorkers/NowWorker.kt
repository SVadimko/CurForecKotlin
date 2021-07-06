package com.vadimko.curforeckotlin.updateWorkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vadimko.curforeckotlin.ui.now.NowViewModel

//воркер для запуска во вьюмодел запросов загрузки данных ЦБ и Тиньков на текущий момент
class NowWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        NowViewModel.loadDataTCS()
        NowViewModel.loadDataCB()
        return Result.success()
    }
}