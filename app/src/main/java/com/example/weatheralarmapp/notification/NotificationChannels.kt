package com.example.weatheralarmapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weatheralarmapp.R

const val CHANNEL_ID_HIGH_PRIORITY = "high_priority"

@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannels(
    context: Context,
    notificationManager: NotificationManager,
) {
    val activeAlarmChannel =
        NotificationChannel(
            CHANNEL_ID_HIGH_PRIORITY,
            context.getString(R.string.allow_alarm_popup_notification),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.alarm_popup_notifications_description)
            enableVibration(true)
        }

    notificationManager.createNotificationChannel(activeAlarmChannel)
}
