package com.yogeshpaliyal.comrade.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yogeshpaliyal.comrade.R
import com.yogeshpaliyal.comrade.ui.dialog.GoogleLoginDialog
import com.yogeshpaliyal.comrade.ui.screen.homepage.HomeViewModel
import com.yogeshpaliyal.comrade.ui.theme.ThemeManager
import com.yogeshpaliyal.comrade.ui.theme.ThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(viewModel: HomeViewModel = hiltViewModel()) {
    val mGoogleServiceHelper by viewModel.getGoogleServiceHelper().collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Get current theme from ThemeManager
    val currentTheme = ThemeManager.currentTheme.value

    val googleAccount = if (mGoogleServiceHelper != null) GoogleSignIn.getLastSignedInAccount(context) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google Drive Section
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.google_drive_backup)) },
                supportingContent = {
                    Text(
                        if (mGoogleServiceHelper == null) {
                            stringResource(id = R.string.login_to_google_drive)
                        } else {
                            stringResource(id = R.string.logged_in_as, googleAccount?.email ?: "...")
                        }
                    )
                },
                leadingContent = {
                    Icon(
                        if (mGoogleServiceHelper == null) Icons.Filled.Login else Icons.Filled.CloudUpload,
                        contentDescription = stringResource(id = R.string.google_drive_backup)
                    )
                },
                modifier = Modifier.clickable {
                    if (mGoogleServiceHelper == null) {
                        showLoginDialog = true
                    }
                }
            )

            if (mGoogleServiceHelper != null) {
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.logout)) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Logout,
                            contentDescription = stringResource(id = R.string.logout)
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.logoutFromGoogleDrive(context)
                    }
                )
            }
            
            HorizontalDivider()
            
            // Theme selection
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.theme)) },
                supportingContent = {
                    Text(
                        when (currentTheme) {
                            ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
                            ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
                            ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
                        }
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Filled.DarkMode,
                        contentDescription = stringResource(id = R.string.theme)
                    )
                },
                modifier = Modifier.clickable {
                    showThemeDialog = true
                }
            )
        }
    }

    if (showLoginDialog) {
        GoogleLoginDialog(
            onSuccess = { account: GoogleSignInAccount ->
                viewModel.setGoogleServiceHelper(account, context)
                showLoginDialog = false
            },
            hideDialog = {
                showLoginDialog = false
            }
        )
    }
    
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { selectedTheme ->
                // Use ThemeManager to set the theme
                ThemeManager.setTheme(context, selectedTheme)
                showThemeDialog = false
            },
            onDismiss = {
                showThemeDialog = false
            }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.theme)) },
        text = {
            Column {
                ThemeOption(
                    text = stringResource(id = R.string.theme_light),
                    selected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )
                
                ThemeOption(
                    text = stringResource(id = R.string.theme_dark),
                    selected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )
                
                ThemeOption(
                    text = stringResource(id = R.string.theme_system),
                    selected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
