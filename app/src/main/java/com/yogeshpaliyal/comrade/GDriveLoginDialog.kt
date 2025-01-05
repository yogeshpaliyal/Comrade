package com.yogeshpaliyal.comrade

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.comrade.databinding.GdriveLoginDialogBinding
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper


class GDriveLoginDialog: androidx.fragment.app.DialogFragment() {
    private lateinit var binding: GdriveLoginDialogBinding

    val TAG: String = "MainActivity"

    private val REQUEST_CODE_SIGN_IN: Int = 1
    private val REQUEST_CODE_OPEN_DOCUMENT: Int = 2

    private var mDriveServiceHelper: DriveServiceHelper? = null

    private var mListener: GDriveListener? = null
    fun setListener(mListener: GDriveListener){
        this.mListener = mListener
        if (mDriveServiceHelper != null) {
            mListener.loginCompleted(mDriveServiceHelper)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        binding = GdriveLoginDialogBinding.inflate(inflater, container, false)


        val mContext = context
        if (mContext != null ){
            val account = GoogleSignIn.getLastSignedInAccount(mContext)
            if (account?.account == null) {

                // Authenticate the user. For most apps, this should be done when the user performs an
                // action that requires Drive access rather than in onCreate.
                requestSignIn();
            } else {
                account.account?.let {
                    mDriveServiceHelper = getDriveServiceHelper(it)
                    mListener?.loginCompleted(mDriveServiceHelper)
                }
            }
        }

        return binding.root
    }

    private fun getDriveServiceHelper(account: Account): DriveServiceHelper {
        val credential =
            GoogleAccountCredential.usingOAuth2(
                this.activity, setOf(DriveScopes.DRIVE_FILE)
            )
        credential.setSelectedAccount(account)

        val googleDriveService =
            Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Drive API Migration")
                .build()

        return DriveServiceHelper(googleDriveService)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && resultData != null) {
                handleSignInResult(resultData)
            }

            REQUEST_CODE_OPEN_DOCUMENT -> if (resultCode == Activity.RESULT_OK && resultData != null) {
                val uri = resultData.data
                if (uri != null) {
                   // openFileFromFilePicker(uri)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    /**
     * Starts a sign-in activity using [.REQUEST_CODE_SIGN_IN].
     */
    private fun requestSignIn() {
        Log.d(TAG, "Requesting sign-in")

        val signInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        val client = GoogleSignIn.getClient(this.requireActivity(), signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->

                googleAccount.account?.let {
                    mDriveServiceHelper = getDriveServiceHelper(it)
                    mListener?.loginCompleted(mDriveServiceHelper)
                }
            }
            .addOnFailureListener { exception: Exception? ->
                Log.e(
                    TAG,
                    "Unable to sign in.",
                    exception
                )
            }
    }

}