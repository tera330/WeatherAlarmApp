package com.example.weatheralarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatheralarmapp.worker.PlayAlarmWorker
import com.example.weatheralarmapp.worker.StopAlarmWorker

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            "START_ALARM" -> {
                val playWorkRequest = OneTimeWorkRequestBuilder<PlayAlarmWorker>().build()
                WorkManager.getInstance(context).enqueue(playWorkRequest)
            }
            "STOP_ALARM" -> {
                val stopWorkRequest = OneTimeWorkRequestBuilder<StopAlarmWorker>().build()
                WorkManager.getInstance(context).enqueue(stopWorkRequest)
            }
        }
    }
}
