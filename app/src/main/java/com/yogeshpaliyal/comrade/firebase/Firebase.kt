package com.yogeshpaliyal.comrade.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions


object Firebase {

    val firebaseInstances = mutableMapOf<String, FirebaseApp>()

    fun initialize(
        context: Context,
        projectId: String,
        applicationId: String,
        apiKey: String,
        forceRecreate: Boolean
    ): FirebaseApp {
        if (forceRecreate != true) {
            firebaseInstances[projectId]?.let {
                return it
            }
        }
        // Initialize Firebase services here
        // This is a placeholder for actual Firebase initialization code
        val options = FirebaseOptions.Builder()
            .setProjectId(projectId) // Required
            .setApplicationId(applicationId) // Required
            .setApiKey(apiKey)
            .build()

        return FirebaseApp.initializeApp(context, options, projectId).also {
            firebaseInstances[projectId] = it
        }
    }
}