package com.omarkrostom.azanEdge.oldApp.widgetProviders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi

class AzanNotificationProvider: BroadcastReceiver() {

    private val notificationChannelTitle = "Azan notifications"
    private val notificationChannelDescription
            = "This channel to provide you with precise azan notifications !"

    private lateinit var mNotificationChannel: NotificationChannel
    private lateinit var mNotificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotificationChannel()
        }
        showNotification(intent?.extras)
    }

    private fun showNotification(extras: Bundle?) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun buildNotificationChannel() {

    }

}