package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.yogeshpaliyal.comrade.common.IA_BACKUP_REQUEST
import com.yogeshpaliyal.comrade.common.SHARING_CONTENT_URI
import com.yogeshpaliyal.comrade.types.BackupStatus
import java.io.File
import java.io.IOException

class BackupBroadcastHandler: IBroadcastReceiverHandler {
    override val type: String
        get() = IA_BACKUP_REQUEST

    override fun handleAction(context: Context, intent: Intent) {
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
}