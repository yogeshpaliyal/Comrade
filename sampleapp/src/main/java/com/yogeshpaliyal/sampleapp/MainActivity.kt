package com.yogeshpaliyal.sampleapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yogeshpaliyal.library.BackupApp
import com.yogeshpaliyal.library.BackupAppListener
import com.yogeshpaliyal.sampleapp.ui.theme.BackupAppTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity(), BackupAppListener {
    private val backupApp by lazy {
        BackupApp(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackupAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @Composable
    fun Greeting(modifier: Modifier = Modifier) {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        Column(modifier = modifier) {
            Text(
                text = if (backupApp.isCompanionAppInstalled()) "Companion app present" else "Companion app is not present"
            )
            Button(onClick = {

                coroutineScope.launch {
                    val newFile = File(context.externalCacheDir, "file.txt")
                    if (newFile.exists().not()) {
                        newFile.createNewFile()
                        newFile.writeText("Hello World")
                    }
                    backupApp.backupApp(newFile)
                }


            }) {
                Text(
                    text = "Backup"
                )
            }
        }
    }

    override fun backupAppIsNotInstalled() {
        Toast.makeText(this, "Companion App is Not Installed", Toast.LENGTH_SHORT).show()
    }

    override fun backupAppIsNotConfigured() {
        Toast.makeText(this, "Companion App is not Setup correctly", Toast.LENGTH_SHORT).show()
    }

    override fun backupCompleted() {
        Toast.makeText(this, "Backup Completed", Toast.LENGTH_SHORT).show()
    }
}