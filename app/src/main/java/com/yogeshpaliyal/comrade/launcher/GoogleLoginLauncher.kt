package com.yogeshpaliyal.comrade.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class GoogleLoginLauncher : ActivityResultContract<GoogleSignInClient, GoogleSignInAccount?>() {

    override fun createIntent(context: Context, input: GoogleSignInClient): Intent {
        return input.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GoogleSignInAccount? {
            return GoogleSignIn.getSignedInAccountFromIntent(intent).result
    }

}
