package com.currencyconverter.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.currencyconverter.data.repository.CurrencyRepository
import com.currencyconverter.widget.CurrencyWidget

class CurrencySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = CurrencyRepository(applicationContext)
            repository.syncRates().getOrThrow()
            
            // Trigger Glance widget update to show fresh rates/timestamp
            CurrencyWidget().updateAll(applicationContext)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
