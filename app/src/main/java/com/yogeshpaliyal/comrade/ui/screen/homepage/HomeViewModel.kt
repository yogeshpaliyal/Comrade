package com.yogeshpaliyal.comrade.ui.screen.homepage

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.di.DatabaseProvider
import com.yogeshpaliyal.comrade.repository.DriveRepository
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import com.yogeshpaliyal.comrade.utils.GoogleSignInManager
import com.yogeshpaliyal.comrade.worker.GDriveWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import data.ComradeBackup
import data.ComradeBackupQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val databaseProvider: DatabaseProvider,
    private val driveRepository: DriveRepository,
    private val googleSignInManager: GoogleSignInManager
) : ViewModel() {

    private val database by databaseProvider

    val listOfBackupFiles: MutableStateFlow<List<ComradeBackup>> = MutableStateFlow<List<ComradeBackup>>(listOf())

    init {
        viewModelScope.launch {
            databaseProvider.databaseRefreshFlow.collect {
                database.comradeBackupQueries.getAllFilesList().asFlow().mapToList(Dispatchers.IO)
                    .collect {
                        listOfBackupFiles.value = it
                    }
            }
        }
    }



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
        googleSignInManager.onUserSignedIn(account)
        setGoogleServiceHelper(DriveServiceHelper(context, account))
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

    fun syncNow(context: Context) {
        viewModelScope.launch {
            try {
                // Option 1: Use repository directly for immediate sync
                driveRepository.syncNow(context)

                // Option 2: Or use WorkManager for background processing
                val syncWorkRequest = OneTimeWorkRequestBuilder<GDriveWorker>().build()
                WorkManager.getInstance(context).enqueue(syncWorkRequest)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error starting sync", e)
            }
        }
    }
}
