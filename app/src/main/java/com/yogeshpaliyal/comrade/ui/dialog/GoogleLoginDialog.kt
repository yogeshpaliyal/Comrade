package com.yogeshpaliyal.comrade.ui.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.comrade.launcher.GoogleLoginLauncher

@Composable
fun GoogleLoginDialog(onSuccess: (googleSignInAccount: GoogleSignInAccount) -> Unit, hideDialog: () -> Unit) {

    val launcher = rememberLauncherForActivityResult(GoogleLoginLauncher()) {
        if (it != null) {
            onSuccess(it)
        } else {
            hideDialog()
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val signInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        val client = GoogleSignIn.getClient(context as AppCompatActivity, signInOptions)

        launcher.launch(client)
    }

    AlertDialog(onDismissRequest = {},
        buttons = {},
        properties = DialogProperties(),
        text = {
            Text("Loading")
        }
        )
}