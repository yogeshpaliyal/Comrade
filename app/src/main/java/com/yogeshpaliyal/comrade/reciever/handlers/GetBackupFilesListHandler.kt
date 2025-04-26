package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILES_LIST
import data.ComradeBackupQueries
import javax.inject.Inject

class GetBackupFilesListHandler @Inject constructor() : IBroadcastReceiverHandler {
    companion object {
        const val ACTION = IA_GET_BACKUP_FILES_LIST
    }
    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
//        val backupFiles = queries.getAllFiles()
        // Assuming query returns a list of file paths
        val resultIntent = Intent().apply {
            action = "com.yogeshpaliyal.comrade.get.backup.files.list.result"
            `package` = callingApp
//            putStringArrayListExtra("backupFilesList", ArrayList(backupFiles.map { it.filePath }))
        }
        context.sendBroadcast(resultIntent)
    }
}