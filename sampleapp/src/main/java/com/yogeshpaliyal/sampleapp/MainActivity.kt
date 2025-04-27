package com.yogeshpaliyal.sampleapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.comrade.library.Comrade
import com.yogeshpaliyal.comrade.library.ComradeListener
import com.yogeshpaliyal.sampleapp.ui.theme.BackupAppTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity(), ComradeListener {
    private val comrade by lazy {
        Comrade(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackupAppTheme {
                SampleAppScreen(
                    isCompanionAppInstalled = comrade.isCompanionAppInstalled(),
                    onBackupClick = { file -> 
                        comrade.backupApp(file)
                    }
                )
            }
        }
    }

    override fun comradeUnavailable() {
        Toast.makeText(this, "Companion App is Not Installed", Toast.LENGTH_SHORT).show()
    }

    override fun comradeIsNotConfigured() {
        Toast.makeText(this, "Companion App is not Setup correctly", Toast.LENGTH_SHORT).show()
    }

    override fun backupCompleted() {
        Toast.makeText(this, "Backup Completed", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppScreen(
    isCompanionAppInstalled: Boolean,
    onBackupClick: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var lastBackupTime by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Backup Sample App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            StatusCard(isCompanionAppInstalled = isCompanionAppInstalled)
            
            // Last Backup Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Last Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lastBackupTime ?: "No backup performed yet",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Backup Button Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create New Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ElevatedButton(
                        onClick = {
                            coroutineScope.launch {
                                val newFile = File(context.externalCacheDir, "file123.txt")
                                if (newFile.exists().not()) {
                                    newFile.createNewFile()
                                    newFile.writeText("Hello World")
                                }
                                onBackupClick(newFile)
                                lastBackupTime = "Just now"
                            }
                        },
                        enabled = isCompanionAppInstalled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Backup Now")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(isCompanionAppInstalled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompanionAppInstalled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val icon: ImageVector
            val statusText: String
            
            if (isCompanionAppInstalled) {
                icon = Icons.Default.CheckCircle
                statusText = "Companion App Installed"
            } else {
                icon = Icons.Default.Warning
                statusText = "Companion App Not Installed"
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isCompanionAppInstalled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCompanionAppInstalled) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
