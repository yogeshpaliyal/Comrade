package com.yogeshpaliyal.comrade.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.IA_COMPANION_SETUP_COMPLETED
import com.yogeshpaliyal.comrade.di.DatabaseProvider
import com.yogeshpaliyal.comrade.reciever.handlers.BackupBroadcastHandler
import com.yogeshpaliyal.comrade.reciever.handlers.GetBackupFileHandler
import com.yogeshpaliyal.comrade.reciever.handlers.GetBackupFileStatusHandler
import com.yogeshpaliyal.comrade.reciever.handlers.GetBackupFilesListHandler
import com.yogeshpaliyal.comrade.reciever.handlers.IBroadcastReceiverHandler
import dagger.hilt.android.AndroidEntryPoint
import data.ComradeBackupQueries
import javax.inject.Inject


@AndroidEntryPoint
class MainAppReceiver : BroadcastReceiver() {

    @Inject
    lateinit var databaseDelegate: DatabaseProvider

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        // Check if main App exists
        // Check if Google Drive Logged in Main App
        val callingApp = intent?.getStringExtra(CLIENT_APP_PACKAGE_NAME) ?: return

        val isSetupComplete = true
        if (!isSetupComplete) {
            // Setup is not complete
            comradeNotConfigured(context, callingApp)
            return
        }

        val request = when(intent.action) {
            BackupBroadcastHandler.ACTION -> BackupBroadcastHandler(databaseDelegate)
            GetBackupFileHandler.ACTION -> GetBackupFileHandler()
            GetBackupFilesListHandler.ACTION -> GetBackupFilesListHandler()
            GetBackupFileStatusHandler.ACTION -> GetBackupFileStatusHandler()
            else -> null
        }

        request?.handleAction(context, intent, callingApp)

    }

    private fun comradeNotConfigured(context: Context?, targetApp: String) {
        val mIntent = Intent()
        mIntent.action = IA_COMPANION_SETUP_COMPLETED
        mIntent.`package` = targetApp
        context?.sendBroadcast(mIntent)
    }



}
