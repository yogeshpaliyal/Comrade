package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILE
import data.ComradeBackupQueries
import javax.inject.Inject

class GetBackupFileHandler @Inject constructor() : IBroadcastReceiverHandler {

    companion object {
        const val ACTION = IA_GET_BACKUP_FILE
    }

    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
        val fileId = intent.getLongExtra("fileId", -1L)
        if (fileId < 0) return

//        val fileInfo = queries.getFileInfo(fileId)
        // Assuming you send back a file's info as a broadcast
//        if (fileInfo != null) {
            val resultIntent = Intent().apply {
                this.action = "com.yogeshpaliyal.comrade.get.backup.file.result"
                `package` = callingApp
//                putExtra("fileInfo", fileInfo.filePath)
            }
            context.sendBroadcast(resultIntent)
//        }
    }
}