package com.yogeshpaliyal.sampleapp

import android.app.Application
import com.yogeshpaliyal.comrade.library.ComradeCrashHandler

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ComradeCrashHandler.createInstance(
            this, BuildConfig.FIREBASE_PROJECT_ID, BuildConfig.FIREBASE_APP_ID,
            BuildConfig.FIREBASE_API_KEY
        )
    }
}