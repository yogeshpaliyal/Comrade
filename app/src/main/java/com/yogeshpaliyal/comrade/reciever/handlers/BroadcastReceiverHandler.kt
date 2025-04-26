package com.yogeshpaliyal.comrade.reciever.handlers

import android.content.Context
import android.content.Intent

interface IBroadcastReceiverHandler {
    fun handleAction(context: Context, intent: Intent, callingApp: String)
}