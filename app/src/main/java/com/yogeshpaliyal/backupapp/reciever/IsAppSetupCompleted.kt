package com.yogeshpaliyal.backupapp.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.common.SETUP_IS_COMPLETED

class IsAppSetupCompleted: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if main App exists
        // Check if Google Drive Logged in Main App
        val callingApp = intent?.getStringExtra(CLIENT_APP_PACKAGE_NAME) ?: return
        val isSetupComplete = false
        val mIntent = Intent()
        mIntent.action = SETUP_IS_COMPLETED
        mIntent.putExtra(SETUP_IS_COMPLETED, isSetupComplete)
        mIntent.`package` = callingApp.toString()
        context?.sendBroadcast(mIntent)
    }
}
