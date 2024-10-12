package com.example.weatheralarmapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatheralarmapp.data.AlarmItem
import com.example.weatheralarmapp.data.AlarmItemRepository
import com.example.weatheralarmapp.dateformat.createHourString
import com.example.weatheralarmapp.dateformat.createMinuteString
import com.example.weatheralarmapp.receiver.AlarmReceiver
import com.example.weatheralarmapp.ui.alarm.AlarmUiState
import com.example.weatheralarmapp.ui.home.HomeUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
class AlarmViewModel(
    application: Application,
    private val alarmItemRepository: AlarmItemRepository,
) : AndroidViewModel(application) {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val currentTime = LocalDateTime.now()
    private val hourStr = createHourString(currentTime.hour)
    private val minutesStr = createMinuteString(currentTime.minute)

    private var _alarmUiState: MutableState<AlarmUiState> =
        mutableStateOf(AlarmUiState(0, "$hourStr:$minutesStr", true))
    val alarmUiState: State<AlarmUiState>
        get() = _alarmUiState

    val homeUiState: StateFlow<HomeUiState> =
        alarmItemRepository
            .getAllAlarmItemsStream()
            .map { HomeUiState(it) }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState(),
            )

    suspend fun addAlarmItem(
        alarmManager: AlarmManager,
        alarmItem: AlarmItem,
    ) {
        alarmItemRepository.insertAlarmItem(alarmItem)
        setAlarm(alarmManager, alarmItem)
    }

    suspend fun updateAlarmItem(
        alarmManager: AlarmManager,
        alarmItem: AlarmItem,
    ) {
        alarmItemRepository.updateAlarmItem(alarmItem)

        if (alarmItem.isAlarmOn) {
            setAlarm(
                alarmManager,
                AlarmItem(alarmItem.id, alarmItem.alarmTime, true),
            )
        } else {
            cancelAlarm(
                alarmManager,
                AlarmItem(alarmItem.id, alarmItem.alarmTime, false),
            )
        }
    }

    suspend fun deleteAlarmItem(
        alarmItem: AlarmItem,
        alarmManager: AlarmManager,
    ) {
        cancelAlarm(
            alarmManager,
            alarmItem,
        )
        alarmItemRepository.deleteAlarmItem(alarmItem)
    }

    private fun setAlarm(
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
                PendingIntent.FLAG_IMMUTABLE,
            )
        val calendar: Calendar =
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmItem.alarmTime.substringBefore(":").toInt())
                set(Calendar.MINUTE, alarmItem.alarmTime.substringAfter(":").toInt())
                set(Calendar.SECOND, 0)
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

    private fun cancelAlarm(
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
                PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }
}
