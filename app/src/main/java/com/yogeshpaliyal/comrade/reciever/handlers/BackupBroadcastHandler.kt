package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.yogeshpaliyal.comrade.common.IA_BACKUP_ADDED_TO_QUEUE
import com.yogeshpaliyal.comrade.common.IA_BACKUP_REQUEST
import com.yogeshpaliyal.comrade.common.SHARING_CONTENT_URI
import com.yogeshpaliyal.comrade.types.BackupStatus
import data.ComradeBackupQueries
import java.io.File
import java.io.IOException
import javax.inject.Inject

class BackupBroadcastHandler @Inject constructor(): IBroadcastReceiverHandler {
    override val type: String
        get() = IA_BACKUP_REQUEST

    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
        val contentUri = intent.getStringExtra(SHARING_CONTENT_URI)

        if (contentUri != null) {
            // Use the receivedUri to access the file.
            val newFile = File(context?.cacheDir, "NewFile.txt")
            try {
                newFile.outputStream().use { output ->
                    context?.contentResolver?.openInputStream(Uri.parse(contentUri))?.use {
                        it.copyTo(output)
                    }
                }

                val sig =
                    context?.packageManager?.getPackageInfo(
                        callingApp,
                        PackageManager.GET_SIGNATURES
                    )?.signatures?.firstOrNull()

                queries.insertFileInfo(
                    callingApp,
                    sig?.toCharsString() ?: "",
                    null,
                    newFile.path,
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