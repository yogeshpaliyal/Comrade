package com.yogeshpaliyal.comrade.repository

import android.content.Context
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.types.BackupStatus
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: Database
) {

    fun getDriveServiceHelper(): DriveServiceHelper? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null

        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        val drive = com.google.api.services.drive.Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Comrade Backup")
            .build()

        return DriveServiceHelper(drive)
    }

    suspend fun syncAllBackups(context: Context) = withContext(Dispatchers.IO) {
        val driveServiceHelper = getDriveServiceHelper() ?: return@withContext

        // Get all pending backups from the database
        val pendingBackups = database.comradeBackupQueries.getAllPendingFiles().executeAsList()

        // Upload each pending backup
        pendingBackups.forEach { backup ->
            try {
                // Perform the actual backup to Drive
                // This is a simplified example - implement the actual upload logic
                // val filePath = backup.filePath ?: return@forEach
                database.comradeBackupQueries.changeBackupStatus(
                    BackupStatus.BACKUP_IN_PROGRESS,
                    backup.id
                )
                val file = File(backup.localFilePath)
                val mimeType = context.contentResolver.getType(backup.localFilePath.toUri())
                val fileId = driveServiceHelper.createFile(file.name, mimeType)
                fileId?.let {
                    driveServiceHelper.saveFile(it, file.name, mimeType, file)
                    database.comradeBackupQueries.changeBackupStatus(
                        BackupStatus.BACKUP_COMPLETED,
                        backup.id
                    )
                }

                // Update backup status in database after successful upload

            } catch (e: Exception) {
                // Mark as failed in case of error
                println(e)
                database.comradeBackupQueries.changeBackupStatus(
                    BackupStatus.BACKUP_FAILED,
                    backup.id
                )
            }
        }
    }

    suspend fun syncNow(context: Context) = withContext(Dispatchers.IO) {
        syncAllBackups(context)
    }
}
