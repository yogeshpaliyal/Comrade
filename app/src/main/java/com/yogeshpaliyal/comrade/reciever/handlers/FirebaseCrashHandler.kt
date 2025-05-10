package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent
import com.yogeshpaliyal.comrade.common.IA_FIREBASE_CRASH
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILE
import com.yogeshpaliyal.comrade.common.IA_GET_BACKUP_FILE_STATUS
import data.ComradeBackupQueries
import javax.inject.Inject

class FirebaseCrashHandler @Inject constructor() : IBroadcastReceiverHandler {
    companion object {
        const val ACTION = IA_FIREBASE_CRASH
    }
    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
//        FirebaseApp
//        context.sendBroadcast(resultIntent)

//        Firebase
    }
}