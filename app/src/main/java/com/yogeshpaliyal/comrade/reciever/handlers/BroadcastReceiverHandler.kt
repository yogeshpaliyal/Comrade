package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent

interface IBroadcastReceiverHandler {
    val type: String
    fun handleAction(context: Context, intent: Intent)
}