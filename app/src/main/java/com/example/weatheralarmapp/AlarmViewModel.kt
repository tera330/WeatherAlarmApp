package com.example.weatheralarmapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatheralarmapp.data.AlarmItem
import com.example.weatheralarmapp.data.AlarmItemRepository
import com.example.weatheralarmapp.data.GetWeatherRepositoryImpl
import com.example.weatheralarmapp.dateformat.createHourString
import com.example.weatheralarmapp.dateformat.createMinuteString
import com.example.weatheralarmapp.receiver.AlarmReceiver
import com.example.weatheralarmapp.ui.alarm.AlarmUiState
import com.example.weatheralarmapp.ui.home.HomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
class AlarmViewModel(
    application: Application,
    private val alarmItemRepository: AlarmItemRepository,
    private val getWeatherRepositoryImpl: GetWeatherRepositoryImpl,
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
        mutableStateOf(AlarmUiState(0, "$hourStr:$minutesStr", true, false))
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

    private var _coordinateState: MutableState<CoordinateState> = mutableStateOf(CoordinateState.Initial)
    private var _weatherState: MutableState<WeatherState> = mutableStateOf(WeatherState.Initial)
    val weatherState: State<WeatherState>
        get() = _weatherState

    private val FIRST_FORECAST_TIME = 6

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
                AlarmItem(alarmItem.id, alarmItem.alarmTime, true, alarmItem.isWeatherForecastOn),
            )
        } else {
            cancelAlarm(
                alarmManager,
                AlarmItem(alarmItem.id, alarmItem.alarmTime, false, alarmItem.isWeatherForecastOn),
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

    fun getWeatherByCityName(
        cityName: String,
        alarmTime: LocalTime,
    ) {
        viewModelScope.launch {
            _coordinateState.value = CoordinateState.Loading
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        getWeatherRepositoryImpl.getCoordinate(cityName)
                    }
                _coordinateState.value = CoordinateState.Success(result.lat, result.lon)
                // アラームの時間が現在時刻よりも前であれば次の日の時刻とする
                // 6時から3時間おきに天気情報を取得する
                val cnt =
                    if (alarmTime.hour < currentTime.hour) {
                        (alarmTime.hour + 24 - FIRST_FORECAST_TIME) / 3 + 1
                    } else {
                        (alarmTime.hour - FIRST_FORECAST_TIME) / 3 + 1
                    }
                getWeatherByLocation(result.lat, result.lon, cnt)
            } catch (e: Exception) {
                _coordinateState.value = CoordinateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getWeatherByLocation(
        lat: Double,
        lon: Double,
        cnt: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _weatherState.value = WeatherState.Loading
            try {
                val result = getWeatherRepositoryImpl.getWeather(lat, lon, cnt)
                _weatherState.value =
                    WeatherState.Success(
                        result.list
                            .last()
                            .weather[0]
                            .description,
                    )
            } catch (e: Exception) {
                Log.d("result", e.message.toString())
                _weatherState.value = WeatherState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface CoordinateState {
    data object Initial : CoordinateState

    data object Loading : CoordinateState

    data class Success(
        val lat: Double,
        val lon: Double,
    ) : CoordinateState

    data class Error(
        val message: String,
    ) : CoordinateState
}

sealed interface WeatherState {
    data object Initial : WeatherState

    data object Loading : WeatherState

    data class Success(
        val weather: String,
    ) : WeatherState

    data class Error(
        val message: String,
    ) : WeatherState
}
