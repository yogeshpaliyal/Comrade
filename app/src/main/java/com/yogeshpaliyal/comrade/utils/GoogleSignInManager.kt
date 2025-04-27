package com.yogeshpaliyal.comrade.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.yogeshpaliyal.comrade.repository.DriveRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val driveRepository: DriveRepository,
) {
    companion object {
        private const val TAG = "GoogleSignInManager"
    }

    fun logout(onSuccess: () -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Ensure you request scopes needed for sign-in options
            // Add other scopes if needed, like .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Clear the DriveServiceHelper upon successful sign out
            onSuccess()
        }
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
                        driveRepository.downloadMissingFiles(context)
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
