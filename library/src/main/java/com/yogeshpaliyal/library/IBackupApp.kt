package com.yogeshpaliyal.library

interface IBackupApp {
    fun backupApp()
    fun restoreApp()
    fun isCompanionAppInstalled(): Boolean
}