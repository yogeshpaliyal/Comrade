package com.yogeshpaliyal.comrade.library

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.comrade.common.CLIENT_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.COMPANION_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.IA_FIREBASE_CRASH

fun createComradeInitialIntent(context: Context, action: String): Intent = Intent().also { intent ->
    intent.component = ComponentName(
        COMPANION_APP_PACKAGE_NAME,
        "com.yogeshpaliyal.comrade.reciever.MainAppReceiver"
    )
    intent.action = action
    intent.`package` = COMPANION_APP_PACKAGE_NAME
    intent.putExtra(CLIENT_APP_PACKAGE_NAME, context.packageName)
}