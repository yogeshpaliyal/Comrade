package com.yogeshpaliyal.comrade.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.yogeshpaliyal.comrade.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.IA_BACKUP_ADDED_TO_QUEUE
import com.yogeshpaliyal.comrade.common.IA_COMPANION_SETUP_COMPLETED
import com.yogeshpaliyal.comrade.common.SHARING_CONTENT_URI
import com.yogeshpaliyal.comrade.types.BackupStatus
import dagger.hilt.android.AndroidEntryPoint
import data.ComradeBackupQueries
import java.io.File
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class MainAppReceiver: BroadcastReceiver() {

    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if main App exists
        // Check if Google Drive Logged in Main App
        val callingApp = intent?.getStringExtra(CLIENT_APP_PACKAGE_NAME) ?: return

        val isSetupComplete = true

        if (isSetupComplete) {
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


                    queries.insertFileInfo(callingApp, sig?.toCharsString() ?: "", null, newFile.path, System.currentTimeMillis(), BackupStatus.BACKUP_PENDING)

                    backupAddedToQueue(context, callingApp)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            if (!isSetupComplete) {
                comradeNotConfigured(context, callingApp)
            }
        }


    }

    private fun comradeNotConfigured(context: Context?, targetApp: String){
        val mIntent = Intent()
        mIntent.action = IA_COMPANION_SETUP_COMPLETED
        mIntent.`package` = targetApp
        context?.sendBroadcast(mIntent)
    }

    private fun backupAddedToQueue(context: Context?, targetApp: String){
        val mIntent = Intent()
        mIntent.action = IA_BACKUP_ADDED_TO_QUEUE
        mIntent.`package` = targetApp
        context?.sendBroadcast(mIntent)
    }

}
