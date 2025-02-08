package com.yogeshpaliyal.comrade.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yogeshpaliyal.comrade.ui.dialog.GoogleLoginDialog
import com.yogeshpaliyal.comrade.ui.screen.homepage.HomeViewModel


@Composable
fun SettingsPage(viewModel: HomeViewModel = hiltViewModel()) {
    val mGoogleServiceHelper = viewModel.getGoogleServiceHelper().collectAsState().value

    val (loginLoading, setLoginLoading) = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column {
        Text("Settings Page")
        if (mGoogleServiceHelper == null) {
            TextButton(onClick = {
                setLoginLoading(true)
            }) {
                Text("Login to google Drive")
            }
        } else {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(LocalContext.current)
            Text("Logged in as ${googleAccount?.account?.name}")
        }
    }
    if (loginLoading) {
        GoogleLoginDialog({
            viewModel.setGoogleServiceHelper(it, context)
            setLoginLoading(false)
        }) {
            setLoginLoading(false)
        }
    }

}