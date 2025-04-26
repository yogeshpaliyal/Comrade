package com.yogeshpaliyal.comrade.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WorkManagerInitializer {

    fun syncNow(context: Context) {
        val constraints = Constraints.Builder()
            //.setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailySyncRequest = OneTimeWorkRequestBuilder<GDriveWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "gdrive_sync",
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            dailySyncRequest
        )
    }

    private fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailySyncRequest = PeriodicWorkRequestBuilder<GDriveWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "gdrive_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            dailySyncRequest
        )
    }
}
