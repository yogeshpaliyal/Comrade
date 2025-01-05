package com.yogeshpaliyal.library

interface BackupAppListener {
    fun backupAppIsNotInstalled()
    fun backupAppIsNotConfigured()
    fun backupCompleted()
}