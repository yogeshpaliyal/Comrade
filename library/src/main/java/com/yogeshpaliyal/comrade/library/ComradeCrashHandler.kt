package com.yogeshpaliyal.comrade.library

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.yogeshpaliyal.comrade.common.COMPANION_APP_PACKAGE_NAME
import com.yogeshpaliyal.comrade.common.FIREBASE_API_KEY
import com.yogeshpaliyal.comrade.common.FIREBASE_APPLICATION_ID
import com.yogeshpaliyal.comrade.common.FIREBASE_EVENT_TYPE
import com.yogeshpaliyal.comrade.common.FIREBASE_LOG_KEY
import com.yogeshpaliyal.comrade.common.FIREBASE_LOG_MESSAGE
import com.yogeshpaliyal.comrade.common.FIREBASE_LOG_VALUE
import com.yogeshpaliyal.comrade.common.FIREBASE_PROJECT_ID
import com.yogeshpaliyal.comrade.common.FIREBASE_THROWABLE
import com.yogeshpaliyal.comrade.common.FirebaseEventType
import com.yogeshpaliyal.comrade.common.IA_FIREBASE_CRASH


class ComradeCrashHandler private constructor(
    private val mContext: Context,
    private val projectId: String,
    private val applicationId: String,
    private val apiKey: String
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: ComradeCrashHandler? = null
        fun createInstance(
            context: Context,
            projectId: String,
            applicationId: String,
            apiKey: String
        ): ComradeCrashHandler {
            instance = ComradeCrashHandler(context, projectId, applicationId, apiKey)
            return instance!!
        }

        fun getInstance(): ComradeCrashHandler? {
            return instance
                ?: throw IllegalStateException("ComradeCrashHandler is not initialized. Call createInstance() first.")
        }
    }

    init {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        val newExceptionHandler = Thread.UncaughtExceptionHandler { thread, exception ->
            recordError(exception)
            prev?.uncaughtException(thread, exception)
        }
        Thread.setDefaultUncaughtExceptionHandler(newExceptionHandler)
        resetFirebaseInstance()
        // Setup Default Values
        setKeyValue("app_version", mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName ?: "Unknown")
        setKeyValue("app_version_code", mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionCode)
    }

    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
//                IA_BACKUP_ADDED_TO_QUEUE -> mListener.backupCompleted()
//                IA_COMPANION_SETUP_COMPLETED -> mListener.comradeIsNotConfigured()
            }
        }
    }


    private fun createFirebaseIntent(): Intent {
        return createComradeInitialIntent(mContext, IA_FIREBASE_CRASH).apply {
            putExtra(FIREBASE_PROJECT_ID, projectId)
            putExtra(FIREBASE_APPLICATION_ID, applicationId)
            putExtra(FIREBASE_API_KEY, apiKey)
        }
    }

    private fun recordError(throwable: Throwable) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_PROJECT_ID, projectId)
        intent.putExtra(FIREBASE_APPLICATION_ID, applicationId)
        intent.putExtra(FIREBASE_API_KEY, apiKey)
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.CRASH)
        intent.putExtra(FIREBASE_THROWABLE, throwable)
        mContext.sendBroadcast(intent)
    }


    private fun resetFirebaseInstance() {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_PROJECT_ID, projectId)
        intent.putExtra(FIREBASE_APPLICATION_ID, applicationId)
        intent.putExtra(FIREBASE_API_KEY, apiKey)
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.RESET)
        mContext.sendBroadcast(intent)
    }

    fun log(message: String) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.LOG)
        intent.putExtra(FIREBASE_LOG_MESSAGE, message)
        mContext.sendBroadcast(intent)
    }

    fun setKeyValue(key: String, value: String) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.KEY_VALUE)
        intent.putExtra(FIREBASE_LOG_KEY, key)
        intent.putExtra(FIREBASE_LOG_VALUE, value)
        mContext.sendBroadcast(intent)
    }

    fun setKeyValue(key: String, value: Int) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.KEY_VALUE)
        intent.putExtra(FIREBASE_LOG_KEY, key)
        intent.putExtra(FIREBASE_LOG_VALUE, value)
        mContext.sendBroadcast(intent)
    }

    fun setKeyValue(key: String, value: Boolean) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.KEY_VALUE)
        intent.putExtra(FIREBASE_LOG_KEY, key)
        intent.putExtra(FIREBASE_LOG_VALUE, value)
        mContext.sendBroadcast(intent)
    }

    fun setKeyValue(key: String, value: Float) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.KEY_VALUE)
        intent.putExtra(FIREBASE_LOG_KEY, key)
        intent.putExtra(FIREBASE_LOG_VALUE, value)
        mContext.sendBroadcast(intent)
    }

    fun setKeyValue(key: String, value: Long) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.KEY_VALUE)
        intent.putExtra(FIREBASE_LOG_KEY, key)
        intent.putExtra(FIREBASE_LOG_VALUE, value)
        mContext.sendBroadcast(intent)
    }

    fun setKeyValue(key: String, value: Double) {
        val intent = createFirebaseIntent()
        intent.putExtra(FIREBASE_EVENT_TYPE, FirebaseEventType.KEY_VALUE)
        intent.putExtra(FIREBASE_LOG_KEY, key)
        intent.putExtra(FIREBASE_LOG_VALUE, value)
        mContext.sendBroadcast(intent)
    }

    fun isCompanionAppInstalled(): Boolean {
        try {
            mContext.packageManager.getPackageInfo(COMPANION_APP_PACKAGE_NAME, 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }
}
