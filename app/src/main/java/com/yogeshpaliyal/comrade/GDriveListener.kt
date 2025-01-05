package com.yogeshpaliyal.comrade

import com.yogeshpaliyal.comrade.utils.DriveServiceHelper

fun interface GDriveListener {
    fun loginCompleted(mDriveServiceHelper: DriveServiceHelper?)
}