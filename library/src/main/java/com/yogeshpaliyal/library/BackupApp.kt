package com.yogeshpaliyal.library

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.yogeshpaliyal.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.common.COMPANION_APP_PACKAGE_NAME
import com.yogeshpaliyal.common.IS_APP_SETUP_COMPLETED
import com.yogeshpaliyal.common.SETUP_IS_COMPLETED


class BackupApp(private val mContext: Context, private val mListener: BackupAppListener) :
    IBackupApp {
    // Check if main App exists
    // Check if Google Drive Logged in Main App


    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isAppSetup = intent?.getBooleanExtra(SETUP_IS_COMPLETED, false)
            if (isAppSetup?.not() == true) {
                mListener.backupAppIsNotConfigured()
            }
            mContext.unregisterReceiver(this)
        }
    }

    override fun backupApp() {

        if (isPackageInstalled().not()) {
            mListener.backupAppIsNotInstalled()
            return
        }

        val intent = Intent()
        intent.component = ComponentName(
            COMPANION_APP_PACKAGE_NAME,
            "com.yogeshpaliyal.backupapp.reciever.IsAppSetupCompleted"
        )
        intent.action = IS_APP_SETUP_COMPLETED
        intent.`package` = COMPANION_APP_PACKAGE_NAME
        intent.putExtra(CLIENT_APP_PACKAGE_NAME, mContext.packageName)
        mContext.sendBroadcast(intent)

        val intentFilter = IntentFilter()
        intentFilter.addAction(SETUP_IS_COMPLETED)
        ContextCompat.registerReceiver(
            mContext,
            myBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Wait for 1 second if call back is not called then app is not installed
    }

    override fun restoreApp() {

    }

    private fun isPackageInstalled(): Boolean {
        try {
            mContext.packageManager.getPackageInfo(COMPANION_APP_PACKAGE_NAME, 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }

}