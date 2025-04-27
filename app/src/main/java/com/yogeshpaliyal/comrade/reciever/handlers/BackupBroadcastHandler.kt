package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.common.IA_BACKUP_ADDED_TO_QUEUE
import com.yogeshpaliyal.comrade.common.IA_BACKUP_REQUEST
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILE
import com.yogeshpaliyal.comrade.common.SHARING_CONTENT_URI
import com.yogeshpaliyal.comrade.types.BackupStatus
import data.ComradeBackupQueries
import java.io.File
import java.io.IOException
import javax.inject.Inject
import androidx.core.net.toUri
import com.yogeshpaliyal.comrade.di.DatabaseProvider

class BackupBroadcastHandler @Inject constructor(val databaseProvider: DatabaseProvider): IBroadcastReceiverHandler {

    private val database by databaseProvider

    companion object {
        const val ACTION = IA_BACKUP_REQUEST
    }

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
        val contentUri = intent.getStringExtra(SHARING_CONTENT_URI)

        if (contentUri != null) {
            // Use the receivedUri to access the file.
            val fileName = contentUri.split("/").lastOrNull() ?: ""
            val directory = File(context.cacheDir, callingApp)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val newFile = File(directory, fileName)
            try {
                newFile.outputStream().use { output ->
                    context.contentResolver?.openInputStream(contentUri.toUri())?.use {
                        it.copyTo(output)
                    }
                }

                val sig =
                    context.packageManager?.getPackageInfo(
                        callingApp,
                        PackageManager.GET_SIGNATURES
                    )?.signatures?.firstOrNull()

                database.comradeBackupQueries.insertFileInfo(
                    callingApp,
                    sig?.toCharsString() ?: "",
                    null,
                    newFile.path,
                    fileName,
                    System.currentTimeMillis(),
                    BackupStatus.BACKUP_PENDING
                )

                backupAddedToQueue(context, callingApp)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun backupAddedToQueue(context: Context?, targetApp: String) {
        val mIntent = Intent()
        mIntent.action = IA_BACKUP_ADDED_TO_QUEUE
        mIntent.`package` = targetApp
        context?.sendBroadcast(mIntent)
    }
}