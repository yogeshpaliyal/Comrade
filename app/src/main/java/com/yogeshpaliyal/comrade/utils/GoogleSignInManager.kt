package com.yogeshpaliyal.comrade.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yogeshpaliyal.comrade.repository.DriveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInManager @Inject constructor(
    private val driveRepository: DriveRepository
) {
    companion object {
        private const val TAG = "GoogleSignInManager"
    }

    // Call this method when a user successfully signs in
    fun onUserSignedIn(account: GoogleSignInAccount?) {
        if (account != null) {
            Log.d(TAG, "User signed in: ${account.email}")
            
            // Check for database on Google Drive in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val downloaded = driveRepository.searchAndDownloadDatabaseOnLogin()
                    if (downloaded) {
                        Log.d(TAG, "Downloaded and restored database from Google Drive")
                    } else {
                        Log.d(TAG, "No database found on Google Drive or local database is up to date")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking for database on Google Drive", e)
                }
            }
        }
    }
}
