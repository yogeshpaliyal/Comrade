package com.yogeshpaliyal.comrade.library

interface ComradeListener {
    fun comradeUnavailable()
    fun comradeIsNotConfigured()
    fun backupCompleted()
}