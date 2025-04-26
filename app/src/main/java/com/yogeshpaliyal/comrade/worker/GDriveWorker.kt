package com.yogeshpaliyal.comrade.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yogeshpaliyal.comrade.repository.DriveRepository
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GDriveWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val driveRepository: DriveRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "GDriveWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting GDrive sync")
            
            // Check if user is signed in to Google
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            if (account == null) {
                Log.d(TAG, "No Google account found. Skipping sync.")
                return@withContext Result.failure()
            }
            
            // Get DriveServiceHelper instance using the repository
            val driveServiceHelper = driveRepository.getDriveServiceHelper()
            if (driveServiceHelper == null) {
                Log.d(TAG, "Failed to get Drive service helper. Skipping sync.")
                return@withContext Result.retry()
            }
            
            // Perform sync operation
            driveRepository.syncAllBackups(appContext)
            
            Log.d(TAG, "GDrive sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during GDrive sync", e)
            Result.failure()
        }
    }
}
