package com.yogeshpaliyal.comrade.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.work.ListenableWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yogeshpaliyal.comrade.repository.DriveRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GdriveSyncManager @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    private val driveRepository: DriveRepository
) {
    companion object {
        private const val TAG = "GDriveWorker"
    }

    suspend fun sync(isLoginFlow: Boolean = false): ListenableWorker.Result {
        try {
            Log.d(TAG, "Starting GDrive sync")

            // Check if user is signed in to Google
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            if (account == null) {
                Log.d(TAG, "No Google account found. Skipping sync.")
                return ListenableWorker.Result.failure()
            }

            // Get DriveServiceHelper instance using the repository
            val driveServiceHelper = driveRepository.getDriveServiceHelper()
            if (driveServiceHelper == null) {
                Log.d(TAG, "Failed to get Drive service helper. Skipping sync.")
                return ListenableWorker.Result.retry()
            }

            // Only do the regular sync if we didn't just download a new database
            // (which would have updated database references already)
            if (isLoginFlow) {
                driveRepository.searchAndDownloadDatabaseOnLogin()
                // Download missing files from Google Drive
                driveRepository.downloadMissingFiles(applicationContext)
            } else {
                // Perform sync operations
                driveRepository.syncAllBackups()
                driveRepository.syncDatabaseFile(applicationContext)
                driveRepository.downloadMissingFiles(applicationContext)
            }

            Log.d(TAG, "GDrive sync completed successfully")
            setSyncTime()
            return ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during GDrive sync", e)
            return ListenableWorker.Result.failure()
        }
    }

    fun getSyncTime(): Long {
        val preferences =
            applicationContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return preferences.getLong("syncTime", -1)
    }

    fun setSyncTime(syncTime: Long = System.currentTimeMillis()) {
        // Also save to preferences for persistence across app restarts
        val preferences =
            applicationContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        preferences.edit { putLong("syncTime", syncTime) }
    }

    suspend fun downloadDBIfAvailable() {

    }

}