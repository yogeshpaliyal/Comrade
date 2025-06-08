package com.yogeshpaliyal.comrade.common

import androidx.annotation.StringDef

/**
 * Firebase Intent Params
 */
const val FIREBASE_PROJECT_ID = "firebaseProjectId"
const val FIREBASE_APPLICATION_ID = "firebaseApplicationId"
const val FIREBASE_API_KEY = "firebaseApiKey"
const val FIREBASE_EVENT_TYPE = "eventType"

/**
 * Intent Key For Firebase Crash
 */
const val FIREBASE_THROWABLE = "throwable"

/**
 * Intent Key For Firebase Log
 */
const val FIREBASE_LOG_MESSAGE = "logMessage"
const val FIREBASE_LOG_KEY = "logKey"
const val FIREBASE_LOG_VALUE = "logValue"


@StringDef(
    FirebaseEventType.CRASH,
    FirebaseEventType.LOG,
    FirebaseEventType.RESET,
    FirebaseEventType.KEY_VALUE
)
annotation class FirebaseEventType {
    companion object {
        const val CRASH = "CRASH"
        const val RESET = "RESET"
        const val LOG = "LOG"
        const val KEY_VALUE = "KEY_VALUE"
    }
}