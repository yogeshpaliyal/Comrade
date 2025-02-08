package com.yogeshpaliyal.comrade.ui.screen.homepage

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.comrade.types.BackupStatus
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import data.ComradeBackup


private fun initiateGoogleDrive(context: Context): DriveServiceHelper? {
    val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
    return googleAccount?.account?.let {
        val credential =
            GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE)
            )
        credential.setSelectedAccount(it)

        val googleDriveService =
            Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Drive API Migration")
                .build()

        DriveServiceHelper(googleDriveService)
    }
}

@Composable
fun Homepage(viewModel: HomeViewModel = hiltViewModel()) {
    val data = viewModel.listOfBackupFiles.collectAsState(listOf())
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (viewModel.getGoogleServiceHelper().value == null) {
            viewModel.setGoogleServiceHelper(initiateGoogleDrive(context))
        }
    }

    if (data.value.isEmpty()) {
        Text("No Data Found")
    } else {
        LazyColumn {
            items(data.value) {
                BackupCard(it)
            }
        }
    }
}

@Composable
fun BackupCard(item: ComradeBackup, modifier: Modifier = Modifier) {
    val status = when (item.backupStatus) {
        BackupStatus.BACKUP_PENDING -> "Backup Pending"
        BackupStatus.BACKUP_IN_PROGRESS -> "Backup In Progress"
        BackupStatus.BACKUP_COMPLETED -> "Backup Completed"
        BackupStatus.BACKUP_FAILED -> "Backup Failed"
        null -> "Error"
        BackupStatus.UNKNOWN -> "Unknown"
    }

    Card(modifier = Modifier.fillMaxWidth(1f)) {
        Column(modifier = modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    item.packageName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "15 Minutes ago",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    item.filePath ?: "No File Path",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBackupCard() {
    BackupCard(
        ComradeBackup(
            123L,
            "KeyPass",
            "ueiqyriueg",
            "filePath/backup",
            "",
            1L,
            BackupStatus.BACKUP_COMPLETED
        )
    )
}