package com.yogeshpaliyal.comrade.library

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yogeshpaliyal.comrade.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.COMPANION_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.IA_BACKUP_ADDED_TO_QUEUE
import com.yogeshpaliyal.comrade.common.IA_BACKUP_REQUEST
import com.yogeshpaliyal.comrade.common.IA_COMPANION_SETUP_COMPLETED
import com.yogeshpaliyal.comrade.common.SHARING_CONTENT_URI
import java.io.File


class Comrade(private val mContext: Context, private val mListener: ComradeListener) :
    IComrade {

    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                IA_BACKUP_ADDED_TO_QUEUE -> mListener.backupCompleted()
                IA_COMPANION_SETUP_COMPLETED -> mListener.comradeIsNotConfigured()
            }
        }
    }

    override fun backupApp(file: File) {

        if (isCompanionAppInstalled().not()) {
            mListener.comradeUnavailable()
            return
        }


        val contentUri = FileProvider.getUriForFile(
            mContext,
            mContext.applicationContext.packageName + ".fileprovider",
            file
        )
        mContext.grantUriPermission(
            COMPANION_APP_PACKAGE_NAME,
            contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )


        val intent = Intent()
        intent.component = ComponentName(
            COMPANION_APP_PACKAGE_NAME,
            "com.yogeshpaliyal.comrade.reciever.MainAppReceiver"
        )
        intent.action = IA_BACKUP_REQUEST
        intent.`package` = COMPANION_APP_PACKAGE_NAME
        intent.putExtra(CLIENT_APP_PACKAGE_NAME, mContext.packageName)
        intent.putExtra(SHARING_CONTENT_URI, contentUri.toString())
        mContext.sendBroadcast(intent)

        val intentFilter = IntentFilter()
        intentFilter.addAction(IA_COMPANION_SETUP_COMPLETED)
        intentFilter.addAction(IA_BACKUP_ADDED_TO_QUEUE)
        ContextCompat.registerReceiver(
            mContext,
            myBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

    }

    override fun restoreApp() {

    }

    override fun isCompanionAppInstalled(): Boolean {
        try {
            mContext.packageManager.getPackageInfo(COMPANION_APP_PACKAGE_NAME, 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }

}