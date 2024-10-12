package com.example.weatheralarmapp.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weatheralarmapp.AlarmActivity
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.dateformat.createHourString
import com.example.weatheralarmapp.dateformat.createMinuteString
import com.example.weatheralarmapp.receiver.AlarmReceiver
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
fun sendAlarmNotification(
    context: Context,
    notificationManager: NotificationManager,
) {
    val intent =
        Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
    val stopAlarmIntent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
        }

    val activeAlarmPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val stopAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, stopAlarmIntent, PendingIntent.FLAG_IMMUTABLE)
    val currentTime = LocalDateTime.now()

    val activeAlarmNotification =
        Notification
            .Builder(context, CHANNEL_ID_HIGH_PRIORITY)
            .setSmallIcon(R.drawable.baseline_alarm_24)
            .setContentTitle(context.getString(R.string.alarm_title))
            .setContentText(
                context.getString(
                    R.string.alarm_stop_notification_description,
                    createHourString(currentTime.hour),
                    createMinuteString(currentTime.minute),
                ),
            ).setAutoCancel(true)
            .setContentIntent(activeAlarmPendingIntent)
            .setFullScreenIntent(activeAlarmPendingIntent, true)
            .setDeleteIntent(stopAlarmPendingIntent)
            .setCategory(Notification.CATEGORY_ALARM)
            .addAction(
                Notification.Action
                    .Builder(
                        R.drawable.baseline_alarm_24,
                        context.getString(R.string.stop_alarm),
                        stopAlarmPendingIntent,
                    ).build(),
            ).build()

    notificationManager.notify(1, activeAlarmNotification)
}
