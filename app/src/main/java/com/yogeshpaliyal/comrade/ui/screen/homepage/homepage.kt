package com.yogeshpaliyal.comrade.ui.screen.homepage

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    return googleAccount?.let {
        DriveServiceHelper(context, it)
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
        Text(
            "No Backups Found",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            items(data.value) {
                BackupCard(it, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun BackupCard(item: ComradeBackup, modifier: Modifier = Modifier) {
    val (statusText, statusColor) = when (item.backupStatus) {
        BackupStatus.BACKUP_PENDING -> "Pending" to MaterialTheme.colorScheme.secondaryContainer
        BackupStatus.BACKUP_IN_PROGRESS -> "In Progress" to MaterialTheme.colorScheme.tertiaryContainer
        BackupStatus.BACKUP_COMPLETED -> "Completed" to MaterialTheme.colorScheme.primaryContainer
        BackupStatus.BACKUP_FAILED -> "Failed" to MaterialTheme.colorScheme.errorContainer
        BackupStatus.UNKNOWN -> "Unknown" to MaterialTheme.colorScheme.surfaceVariant
        null -> "Error" to MaterialTheme.colorScheme.errorContainer
    }

    val statusTextColor = when (item.backupStatus) {
        BackupStatus.BACKUP_PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
        BackupStatus.BACKUP_IN_PROGRESS -> MaterialTheme.colorScheme.onTertiaryContainer
        BackupStatus.BACKUP_COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
        BackupStatus.BACKUP_FAILED -> MaterialTheme.colorScheme.onErrorContainer
        BackupStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
        null -> MaterialTheme.colorScheme.onErrorContainer
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.fileName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "15 Minutes ago",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.packageName,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusTextColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
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
            "abcd",
            "TestFile.txt",
            "/backup/TestFile.txt",
            1L,
            BackupStatus.BACKUP_COMPLETED
        )
    )
}

