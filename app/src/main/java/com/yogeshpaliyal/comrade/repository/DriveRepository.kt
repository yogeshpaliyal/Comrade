package com.yogeshpaliyal.comrade.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.di.DatabaseProvider
import com.yogeshpaliyal.comrade.types.BackupStatus
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val DB_NAME = "comrade.db"

@Singleton
class DriveRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseProvider: DatabaseProvider
) {
    companion object {
        private const val TAG = "DriveRepository"
        private const val DB_FOLDER_NAME = "comrade_database"
        private const val BACKUP_FOLDER_NAME = "comrade_backups"
        private const val DB_FILE_NAME = "comrade_db.sqlite"
    }

    // Use lateinit to allow this to be set in a non-constructor way
    private val database: Database by databaseProvider
    
    // Interface to notify database connection restart
    interface DatabaseRestartListener {
        fun onDatabaseRestarted()
    }

    private var databaseRestartListener: DatabaseRestartListener? = null

    fun setDatabaseRestartListener(listener: DatabaseRestartListener) {
        databaseRestartListener = listener
    }

    fun getDriveServiceHelper(): DriveServiceHelper? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return DriveServiceHelper(context, account)
    }

    // Function to search and download database when user logs in
    suspend fun searchAndDownloadDatabaseOnLogin(): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveServiceHelper = getDriveServiceHelper() ?: return@withContext false

            // Get the database folder on Drive
            val dbFolderId = driveServiceHelper.createFolderIfNotExists(DB_FOLDER_NAME)

            // Look for the database file
            val driveFiles = driveServiceHelper.listFilesInFolder(dbFolderId)
            val dbDriveFile = driveFiles.find { it.name == DB_FILE_NAME }

            if (dbDriveFile != null) {
                Log.d(TAG, "Found database file on Drive: ${dbDriveFile.name}")

                // Get the current database file
                val currentDbFile = context.getDatabasePath(DB_NAME)

                // Check if local file exists
                val localExists = currentDbFile.exists()

                // Make sure the parent directory exists
                currentDbFile.parentFile?.mkdirs()

                // Temporary file to download to
                val tempDbFile = File(currentDbFile.parentFile, "temp_${DB_NAME}")

                // Download the database file
                val success = driveServiceHelper.downloadFile(dbDriveFile.id, tempDbFile)

                if (success) {
                    // Replace the current database with the downloaded one
                    if (tempDbFile.exists()) {
                        // First close database connections
                        if (localExists) {
                            currentDbFile.delete()
                        }
                        
                        if (tempDbFile.renameTo(currentDbFile)) {
                            // Notify that database has been replaced - this will recreate the DB
                            databaseProvider.resetDatabase()
                            
                            Log.d(TAG, "Database file downloaded and replaced from Drive")
                            return@withContext true
                        }
                    }
                }
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error searching and downloading database", e)
            return@withContext false
        }
    }

    suspend fun syncAllBackups() {
        try {
            val driveServiceHelper = getDriveServiceHelper() ?: return

            // Create or get the backups folder on Google Drive
            val folderId = driveServiceHelper.createFolderIfNotExists(BACKUP_FOLDER_NAME)

            // Get all backup details from local database
            val backups = database.comradeBackupQueries.getAllPendingFiles().executeAsList()

            // Sync each backup file to Google Drive
            backups.forEach { backup ->
                val file = File(backup.localFilePath)
                if (file.exists()) {
                    // If the file exists locally, upload it to Drive
                    driveServiceHelper.uploadFile(file, folderId, "${backup.id}-${backup.fileName}", null)?.let {
                        database.comradeBackupQueries.changeBackupStatus(BackupStatus.BACKUP_COMPLETED, backup.id)
                        database.comradeBackupQueries.setFileId(it, backup.id)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing backups", e)
        }
    }

    suspend fun syncDatabaseFile(context: Context) = withContext(Dispatchers.IO) {
        try {
            val driveServiceHelper = getDriveServiceHelper() ?: return@withContext

            // Create or get the database folder on Google Drive
            val dbFolderId = driveServiceHelper.createFolderIfNotExists(DB_FOLDER_NAME)

            // Get the database file path
            val dbFile = context.getDatabasePath(DB_NAME)
            if (dbFile.exists()) {
                // Upload the database file to Drive
                driveServiceHelper.uploadFile(dbFile, dbFolderId, DB_FILE_NAME, "application/x-sqlite3")
                Log.d(TAG, "Database file synced to Drive")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing database file", e)
        }
    }

    suspend fun downloadMissingFiles(context: Context) = withContext(Dispatchers.IO) {
        try {
            val driveServiceHelper = getDriveServiceHelper() ?: return@withContext

            // Download missing backup files
            downloadMissingBackups(driveServiceHelper)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading missing files", e)
        }
    }

    private suspend fun downloadMissingBackups(driveServiceHelper: DriveServiceHelper) {
        try {
            // Get backup folder
            val folderId = driveServiceHelper.createFolderIfNotExists(BACKUP_FOLDER_NAME)

            // Get all files from the Drive folder
            val driveFiles = driveServiceHelper.listFilesInFolder(folderId)

            // Get all backup details from local database
            val localBackups = database.comradeBackupQueries.getAllFilesList().executeAsList()
            val localFileNames = localBackups.map { it.fileName }

            // Download files that exist on Drive but not locally
            for (driveFile in driveFiles) {
                if (!localFileNames.contains(driveFile.name)) {
                    Log.d(TAG, "Found missing file on Drive: ${driveFile.name}")

                    // Create local backup directory if it doesn't exist
                    val backupDir = File(context.filesDir, "backups")
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }

                    // Download the file
                    val downloadedFile = File(backupDir, driveFile.name)
                    driveServiceHelper.downloadFile(driveFile.id, downloadedFile)

                    Log.d(TAG, "Downloaded missing file: ${driveFile.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading missing backups", e)
        }
    }

    private suspend fun restoreDatabaseFromDrive(driveServiceHelper: DriveServiceHelper, dbFile: File): Boolean {
        try {
            // Get the database folder on Drive
            val dbFolderId = driveServiceHelper.createFolderIfNotExists(DB_FOLDER_NAME)

            // Look for the database file
            val driveFiles = driveServiceHelper.listFilesInFolder(dbFolderId)
            val dbDriveFile = driveFiles.find { it.name == DB_FILE_NAME }

            if (dbDriveFile != null) {
                // Make sure the parent directory exists
                dbFile.parentFile?.mkdirs()

                // Download the database file
                val success = driveServiceHelper.downloadFile(dbDriveFile.id, dbFile)
                if (success) {
                    Log.d(TAG, "Database file restored from Drive")
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring database from Drive", e)
            return false
        }
    }

    // Manually trigger a sync now
    suspend fun syncNow(context: Context) {
        syncAllBackups()
        syncDatabaseFile(context)
        downloadMissingFiles(context)
    }
}
