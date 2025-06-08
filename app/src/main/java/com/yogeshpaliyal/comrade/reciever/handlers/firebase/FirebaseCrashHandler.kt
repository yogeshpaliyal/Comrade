package com.yogeshpaliyal.comrade.reciever.handlers.firebase

import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yogeshpaliyal.comrade.common.FIREBASE_API_KEY
import com.yogeshpaliyal.comrade.common.FIREBASE_APPLICATION_ID
import com.yogeshpaliyal.comrade.common.FIREBASE_ERROR
import com.yogeshpaliyal.comrade.common.FIREBASE_EVENT_TYPE
import com.yogeshpaliyal.comrade.common.FIREBASE_LOG_KEY
import com.yogeshpaliyal.comrade.common.FIREBASE_LOG_MESSAGE
import com.yogeshpaliyal.comrade.common.FIREBASE_LOG_VALUE
import com.yogeshpaliyal.comrade.common.FIREBASE_PROJECT_ID
import com.yogeshpaliyal.comrade.common.FIREBASE_THROWABLE
import com.yogeshpaliyal.comrade.common.FirebaseEventType
import com.yogeshpaliyal.comrade.common.IA_FIREBASE_CRASH
import com.yogeshpaliyal.comrade.common.MESSAGE
import com.yogeshpaliyal.comrade.firebase.Firebase
import com.yogeshpaliyal.comrade.reciever.handlers.IBroadcastReceiverHandler
import data.ComradeBackupQueries
import javax.inject.Inject

class FirebaseCrashHandler @Inject constructor() : IBroadcastReceiverHandler {
    companion object {
        const val ACTION = IA_FIREBASE_CRASH
    }

    @Inject
    lateinit var queries: ComradeBackupQueries

    override fun handleAction(context: Context, intent: Intent, callingApp: String) {
        // Initialize Firebase if not already initialized
        val projectId = intent.getStringExtra(FIREBASE_PROJECT_ID)
        val applicationId = intent.getStringExtra(FIREBASE_APPLICATION_ID)
        val apiKey = intent.getStringExtra(FIREBASE_API_KEY)
        val eventType = intent.getStringExtra(FIREBASE_EVENT_TYPE)

        if (projectId == null || applicationId == null || apiKey == null || eventType == null) {
            handleFirebaseFailure(context, callingApp, "Missing Firebase configuration parameters.")
            return
        }
        val firebase = Firebase.initialize(
            context,
            projectId,
            applicationId,
            apiKey,
            eventType == FirebaseEventType.RESET
        )
        when (eventType) {
            FirebaseEventType.CRASH -> {
                // Handle crash event
                val instance: FirebaseCrashlytics =
                    firebase.get<FirebaseCrashlytics?>(FirebaseCrashlytics::class.java)
                val exception = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra<Throwable>(
                        FIREBASE_THROWABLE,
                        Throwable::class.java
                    )
                } else {
                    intent.getSerializableExtra(FIREBASE_THROWABLE) as? Throwable
                }
                exception?.let { instance.recordException(it) }
            }

            FirebaseEventType.LOG -> {
                // Handle log event
                val message = intent.getStringExtra(FIREBASE_LOG_MESSAGE)
                if (message != null) {
                    val instance: FirebaseCrashlytics =
                        firebase.get<FirebaseCrashlytics?>(FirebaseCrashlytics::class.java)
                    instance.log(message)
                } else {
                    handleFirebaseFailure(context, callingApp, "Missing log parameters.")
                }
            }

            FirebaseEventType.RESET -> {

            }

            FirebaseEventType.KEY_VALUE -> {
                // Handle log event
                val key = intent.getStringExtra(FIREBASE_LOG_KEY)
                val value = intent.extras?.get(FIREBASE_LOG_VALUE)
                if (key != null && value != null) {
                    val instance: FirebaseCrashlytics =
                        firebase.get<FirebaseCrashlytics?>(FirebaseCrashlytics::class.java)
                    when (value) {
                        is String -> instance.setCustomKey(key, value)
                        is Boolean -> instance.setCustomKey(key, value)
                        is Int -> instance.setCustomKey(key, value)
                        is Long -> instance.setCustomKey(key, value)
                        is Float -> instance.setCustomKey(key, value)
                        is Double -> instance.setCustomKey(key, value)
                        else -> handleFirebaseFailure(
                            context,
                            callingApp,
                            "Incorrect type of value for key: $key"
                        )
                    }
                } else {
                    handleFirebaseFailure(context, callingApp, "Missing log key values parameters.")
                }
            }

            else -> {
                handleFirebaseFailure(
                    context,
                    callingApp,
                    "Unknown Firebase event type: $eventType"
                )
            }
        }

    }

    private fun handleFirebaseFailure(context: Context?, targetApp: String, message: String) {
        val mIntent = Intent()
        mIntent.action = FIREBASE_ERROR
        mIntent.`package` = targetApp
        mIntent.putExtra(MESSAGE, message)
        context?.sendBroadcast(mIntent)
    }
}