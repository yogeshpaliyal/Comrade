package com.yogeshpaliyal.comrade.library

import java.io.File

interface IComrade {
    fun backupApp(file: File)
    fun restoreApp()
    fun isCompanionAppInstalled(): Boolean
}