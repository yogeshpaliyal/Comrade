package com.yogeshpaliyal.comrade.library

import java.io.File

interface IComrade {
    fun backupApp(file: File)
    fun recordError(throwable: Throwable)
    fun restoreApp()
    fun isCompanionAppInstalled(): Boolean
}