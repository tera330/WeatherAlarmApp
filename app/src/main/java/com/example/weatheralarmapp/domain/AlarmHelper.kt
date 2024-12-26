package com.example.weatheralarmapp.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class AlarmHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun setAlarm(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
            isBadWeather: Boolean,
        ) {
            val intent =
                Intent(context, AlarmReceiver::class.java).apply {
                    action = "START_ALARM"
                }
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    alarmItem.id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
                )
            val calendar: Calendar =
                if (isBadWeather) {
                    Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(
                            Calendar.HOUR_OF_DAY,
                            alarmItem.changedAlarmTImeByWeather.substringBefore(":").toInt(),
                        )
                        set(
                            Calendar.MINUTE,
                            alarmItem.changedAlarmTImeByWeather.substringAfter(":").toInt(),
                        )
                        set(Calendar.SECOND, 0)
                    }
                } else {
                    Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(
                            Calendar.HOUR_OF_DAY,
                            alarmItem.alarmTime.substringBefore(":").toInt(),
                        )
                        set(
                            Calendar.MINUTE,
                            alarmItem.alarmTime.substringAfter(":").toInt(),
                        )
                        set(Calendar.SECOND, 0)
                    }
                }

            if (calendar.timeInMillis >= System.currentTimeMillis()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent,
                )
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent,
                )
            }
        }

        fun cancelAlarm(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
        ) {
            val intent =
                Intent(context, AlarmReceiver::class.java).apply {
                    action = "START_ALARM"
                }
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    alarmItem.id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
                )
            alarmManager.cancel(pendingIntent)
        }
    }
