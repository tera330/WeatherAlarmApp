package com.example.weatheralarmapp.worker

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatheralarmapp.notification.sendAlarmNotification
import com.example.weatheralarmapp.sound.ExoPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayAlarmWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.Main) {
            val player = ExoPlayerManager.getPlayer(context)
            sendAlarmNotification(
                context,
                notificationManager,
            )
            player.prepare()
            player.play()

            return@withContext try {
                Result.success()
            } catch (throwable: Throwable) {
                Result.failure()
            }
        }
    }
}
