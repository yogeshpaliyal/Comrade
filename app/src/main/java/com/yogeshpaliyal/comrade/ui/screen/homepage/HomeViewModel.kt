package com.yogeshpaliyal.comrade.ui.screen.homepage

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yogeshpaliyal.comrade.di.DatabaseProvider
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import com.yogeshpaliyal.comrade.utils.GdriveSyncManager
import com.yogeshpaliyal.comrade.utils.GoogleSignInManager
import dagger.hilt.android.lifecycle.HiltViewModel
import data.ComradeBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val databaseProvider: DatabaseProvider,
    private val googleSignInManager: GoogleSignInManager,
    private val gdriveSyncManager: GdriveSyncManager
) : ViewModel() {

    private val database by databaseProvider

    val listOfBackupFiles: MutableStateFlow<List<ComradeBackup>> =
        MutableStateFlow<List<ComradeBackup>>(listOf())

    init {
        // Setup data collection that recreates when database changes
        viewModelScope.launch {
            // Use flatMapLatest to re-subscribe to the query when database refreshes
            databaseProvider.databaseRefreshFlow
                .flatMapLatest {
                    Log.d("HomeViewModel", "Database refresh triggered, fetching data...")
                    // This will use the latest database instance
                    database.comradeBackupQueries.getAllFilesList().asFlow()
                        .mapToList(Dispatchers.IO)
                }
                .catch { e -> // Catch errors during flow collection
                    Log.e("HomeViewModel", "Error collecting backup files flow", e)
                    // Optionally update UI state with error
                }
                .collectLatest { backups ->
                    Log.d("HomeViewModel", "Received ${backups.size} backups from database")
                    listOfBackupFiles.value = backups
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
        googleSignInManager.logout {
            setGoogleServiceHelper(null)
        }
        // Optionally, revoke access as well if you want the user to re-authorize next time
        // googleSignInClient.revokeAccess().addOnCompleteListener { ... }
    }

    fun syncNow() {
        viewModelScope.launch {
            gdriveSyncManager.sync()
        }
    }
}
