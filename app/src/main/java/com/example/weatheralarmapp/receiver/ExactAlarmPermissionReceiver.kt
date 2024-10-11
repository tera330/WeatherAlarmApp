package com.example.weatheralarmapp.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ExactAlarmPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            val alarmManager = context.applicationContext.getSystemService(AlarmManager::class.java) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canScheduleAlarms = alarmManager.canScheduleExactAlarms()
                if (canScheduleAlarms) {
                    // TODO アラームの再スケジュール
                } else { }
            }
        }
    }
}
