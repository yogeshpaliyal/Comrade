package com.yogeshpaliyal.library

import java.io.File

interface IBackupApp {
    fun backupApp(file: File)
    fun restoreApp()
    fun isCompanionAppInstalled(): Boolean
}