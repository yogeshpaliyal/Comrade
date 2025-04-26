package com.yogeshpaliyal.comrade.ui.screen.homepage

import android.content.Context
import androidx.lifecycle.ViewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import data.ComradeBackupQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val comradeQueries: ComradeBackupQueries) :
    ViewModel() {

    val listOfBackupFiles = comradeQueries.getAllFilesList().asFlow().mapToList(Dispatchers.IO)

    private var mGoogleServiceHelper: MutableStateFlow<DriveServiceHelper?> = MutableStateFlow(null)

    // Keep this private setter if you only want to set it internally or via the account method
    fun setGoogleServiceHelper(mGoogleServiceHelper: DriveServiceHelper?) {
        this.mGoogleServiceHelper.value = mGoogleServiceHelper
    }

    fun getGoogleServiceHelper(): StateFlow<DriveServiceHelper?> {
        return mGoogleServiceHelper
    }

    // This function is called by the GoogleLoginDialog upon successful sign-in
    fun setGoogleServiceHelper(account: GoogleSignInAccount, context: Context) {
        val credential =
            GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE)
            )
        credential.selectedAccount = account.account // Use property access

        val googleDriveService =
            Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Comrade App") // Set a more specific app name
                .build()

        setGoogleServiceHelper(DriveServiceHelper(googleDriveService))
    }

    // Function to handle logout
    fun logoutFromGoogleDrive(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Ensure you request scopes needed for sign-in options
            // Add other scopes if needed, like .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Clear the DriveServiceHelper upon successful sign out
            setGoogleServiceHelper(null)
        }
        // Optionally, revoke access as well if you want the user to re-authorize next time
        // googleSignInClient.revokeAccess().addOnCompleteListener { ... }
    }
}