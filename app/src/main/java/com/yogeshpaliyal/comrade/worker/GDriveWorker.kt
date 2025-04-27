package com.yogeshpaliyal.comrade.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yogeshpaliyal.comrade.repository.DriveRepository
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import com.yogeshpaliyal.comrade.utils.GdriveSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GDriveWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    val gdriveSyncManager: GdriveSyncManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "GDriveWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
       gdriveSyncManager.sync()
    }
}
