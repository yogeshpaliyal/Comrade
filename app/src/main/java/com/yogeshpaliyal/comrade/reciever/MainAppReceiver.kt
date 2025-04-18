package com.yogeshpaliyal.comrade.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.yogeshpaliyal.comrade.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.IA_BACKUP_ADDED_TO_QUEUE
import com.yogeshpaliyal.comrade.common.IA_BACKUP_REQUEST
import com.yogeshpaliyal.comrade.common.IA_COMPANION_SETUP_COMPLETED
import com.yogeshpaliyal.comrade.reciever.handlers.BackupBroadcastHandler
import com.yogeshpaliyal.comrade.reciever.handlers.IBroadcastReceiverHandler
import dagger.hilt.android.AndroidEntryPoint
import data.ComradeBackupQueries
import javax.inject.Inject


@AndroidEntryPoint
class MainAppReceiver : BroadcastReceiver() {

    @Inject
    lateinit var queries: ComradeBackupQueries

    @Inject
    lateinit var backupBroadcastHandler: BackupBroadcastHandler

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

        // Validate the calling app signature
        val request: IBroadcastReceiverHandler? = when(intent.action) {
            IA_BACKUP_REQUEST -> {
                backupBroadcastHandler
            }
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
