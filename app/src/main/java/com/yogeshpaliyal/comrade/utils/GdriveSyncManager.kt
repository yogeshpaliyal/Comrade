package com.yogeshpaliyal.comrade.utils

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yogeshpaliyal.comrade.repository.DriveRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GdriveSyncManager@Inject constructor(@ApplicationContext val applicationContext: Context, private val driveRepository: DriveRepository) {
    companion object {
        private const val TAG = "GDriveWorker"
    }
    suspend fun sync(): ListenableWorker.Result {
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

            // First check if database exists on drive and download it if newer
            val dbDownloaded = driveRepository.searchAndDownloadDatabaseOnLogin()

            // Only do the regular sync if we didn't just download a new database
            // (which would have updated database references already)
            if (!dbDownloaded) {
                // Sync database file
                driveRepository.syncDatabaseFile(applicationContext)

                // Perform sync operations
                driveRepository.syncAllBackups()

                // Download missing files from Google Drive
                driveRepository.downloadMissingFiles(applicationContext)
            }

            Log.d(TAG, "GDrive sync completed successfully")
            return ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during GDrive sync", e)
            return ListenableWorker.Result.failure()
        }
    }
}