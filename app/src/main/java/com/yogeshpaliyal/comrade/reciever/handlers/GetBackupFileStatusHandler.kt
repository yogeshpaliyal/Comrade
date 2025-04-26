package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILE
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILE_STATUS
import data.ComradeBackupQueries
import javax.inject.Inject

class GetBackupFileStatusHandler @Inject constructor() : IBroadcastReceiverHandler {
    companion object {
        const val ACTION = IA_GET_BACKUP_FILE_STATUS
    }
    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
        val fileId = intent.getLongExtra("fileId", -1L)
        if (fileId < 0) return

//        val fileStatus = queries.getFileStatus(fileId) ?: BackupStatus.UNKNOWN
        val resultIntent = Intent().apply {
            action = "com.yogeshpaliyal.comrade.get.backup.file.status.result"
            `package` = callingApp
//            putExtra("fileStatus", fileStatus.status)
        }
        context.sendBroadcast(resultIntent)
    }
}