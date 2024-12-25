package com.example.weatheralarmapp.ui.features.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.data.repository.GetWeatherRepositoryImpl
import com.example.weatheralarmapp.receiver.AlarmReceiver
import com.example.weatheralarmapp.util.dateformat.createHourString
import com.example.weatheralarmapp.util.dateformat.createMinuteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class AlarmViewModel(
    application: Application,
    private val alarmItemRepository: AlarmItemRepository,
    private val getWeatherRepositoryImpl: GetWeatherRepositoryImpl,
) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val currentTime = LocalDateTime.now()
    private val hourStr = createHourString(currentTime.hour)
    private val minutesStr = createMinuteString(currentTime.minute)

    private var _alarmUiState: MutableStateFlow<AlarmUiState> =
        MutableStateFlow(
            AlarmUiState(
                alarmItemState =
                    AlarmItemState(
                        id = 0,
                        alarmTime = "$hourStr:$minutesStr",
                        selectedEarlyAlarmTime = "00:00",
                        changedAlarmTImeByWeather = "$hourStr:$minutesStr",
                    ),
                weatherState = WeatherState.Initial,
                coordinateState = CoordinateState.Initial,
                expandedAlarmItem = false,
                hoursUntilAlarm = 0L,
                minutesUntilAlarm = 0L,
            ),
        )
    val alarmUiState: StateFlow<AlarmUiState>
        get() = _alarmUiState.asStateFlow()

    private var _homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // リポジトリからのフローを収集して _homeUiState を更新
            alarmItemRepository
                .getAllAlarmItemsStream()
                .map { alarmItems ->
                    // リポジトリから受け取ったデータを HomeUiState に変換
                    HomeUiState(
                        alarmItemList =
                            alarmItems.map { alarmItem ->
                                AlarmUiState(
                                    alarmItemState =
                                        AlarmItemState(
                                            id = alarmItem.id,
                                            alarmTime = alarmItem.alarmTime,
                                            selectedEarlyAlarmTime = alarmItem.selectedEarlyAlarmTime,
                                            changedAlarmTImeByWeather = alarmItem.changedAlarmTImeByWeather,
                                            isAlarmOn = alarmItem.isAlarmOn,
                                            isWeatherForecastOn = alarmItem.isWeatherForecastOn,
                                        ),
                                )
                            },
                    )
                }.collect { updatedHomeUiState ->
                    _homeUiState.value = updatedHomeUiState
                }
        }
    }

    private val FIRST_FORECAST_TIME = 6

    fun expandedAlarmItem() {
        _homeUiState.update {
            it.copy(
                alarmItemList =
                    it.alarmItemList.map { alarmUiState ->
                        alarmUiState.copy(
                            expandedAlarmItem = !alarmUiState.expandedAlarmItem,
                        )
                    },
            )
        }
    }

    fun updateUntilAlarmTime(
        id: Int,
        hoursUntilAlarm: Long,
        minutesUntilAlarm: Long,
    ) {
        _homeUiState.update {
            it.copy(
                alarmItemList =
                    it.alarmItemList.map { alarmUiState ->
                        if (alarmUiState.alarmItemState.id == id) {
                            alarmUiState.copy(
                                hoursUntilAlarm = hoursUntilAlarm,
                                minutesUntilAlarm = minutesUntilAlarm,
                            )
                        } else {
                            alarmUiState
                        }
                    },
            )
        }
    }

    fun updateUntilAlarmTimeByWeather(
        id: Int,
        earlyHoursUntilAlarm: Long,
        earlyMinutesUntilAlarm: Long,
    ) {
        _homeUiState.update {
            it.copy(
                alarmItemList =
                    it.alarmItemList.map { alarmUiState ->
                        if (alarmUiState.alarmItemState.id == id) {
                            alarmUiState.copy(
                                earlyHoursUntilAlarmByWeather = earlyHoursUntilAlarm,
                                earlyMinutesUntilAlarmByWeather = earlyMinutesUntilAlarm,
                            )
                        } else {
                            alarmUiState
                        }
                    },
            )
        }
    }

    suspend fun addAlarmItem(
        alarmManager: AlarmManager,
        alarmItem: AlarmItem,
    ) {
        alarmItemRepository.insertAlarmItem(alarmItem)
        setAlarm(alarmManager, alarmItem, false)
    }

    suspend fun updateAlarmItem(
        alarmManager: AlarmManager,
        alarmItem: AlarmItem,
        isBadWeather: Boolean,
    ) {
        alarmItemRepository.updateAlarmItem(alarmItem)

        // アラームをオンにした時
        if (alarmItem.isAlarmOn && !alarmItem.isWeatherForecastOn) {
            val changedAlarmByWeather = alarmItem.alarmTime
            setAlarm(
                alarmManager,
                AlarmItem(
                    alarmItem.id,
                    alarmItem.alarmTime,
                    changedAlarmByWeather,
                    "0",
                    true,
                    isWeatherForecastOn = false,
                ),
                isBadWeather,
            )
            // 天気予報をオンにした時
        } else if (alarmItem.isAlarmOn && alarmItem.isWeatherForecastOn) {
            setAlarm(
                alarmManager,
                AlarmItem(
                    alarmItem.id,
                    alarmItem.alarmTime,
                    alarmItem.changedAlarmTImeByWeather,
                    selectedEarlyAlarmTime = "0",
                    isAlarmOn = true,
                    isWeatherForecastOn = true,
                ),
                isBadWeather,
            )
            // アラームをオフにした時
        } else {
            cancelAlarm(
                alarmManager,
                AlarmItem(
                    alarmItem.id,
                    alarmItem.alarmTime,
                    alarmItem.changedAlarmTImeByWeather,
                    "0",
                    false,
                    alarmItem.isWeatherForecastOn,
                ),
            )
        }
    }

    suspend fun deleteAlarmItem(
        alarmUiState: AlarmUiState,
        alarmManager: AlarmManager,
    ) {
        val alarmItem = alarmUiState.alarmItemState.toAlarmItem(alarmUiState.alarmItemState)

        cancelAlarm(
            alarmManager,
            alarmItem,
        )
        alarmItemRepository.deleteAlarmItem(alarmItem)
    }

    private fun AlarmItemState.toAlarmItem(alarmItemState: AlarmItemState): AlarmItem =
        AlarmItem(
            id = alarmItemState.id,
            alarmTime = alarmItemState.alarmTime,
            changedAlarmTImeByWeather = alarmItemState.changedAlarmTImeByWeather,
            selectedEarlyAlarmTime = alarmItemState.selectedEarlyAlarmTime,
            isAlarmOn = alarmItemState.isAlarmOn,
            isWeatherForecastOn = alarmItemState.isWeatherForecastOn,
        )

    private fun setAlarm(
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
                PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
            )
        alarmManager.cancel(pendingIntent)
    }

    fun getWeatherByCityName(
        id: Int,
        cityName: String,
        alarmTime: LocalTime,
    ) {
        viewModelScope.launch {
            updateAlarmUiState(id) {
                it.copy(
                    coordinateState = CoordinateState.Loading,
                )
            }
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        getWeatherRepositoryImpl.getCoordinate(cityName)
                    }
                updateAlarmUiState(id) {
                    it.copy(
                        coordinateState = CoordinateState.Success(result.lat, result.lon),
                    )
                }
                // TODO 取得開始時刻が不安定のため調査必要。それに応じてcntの計算を修正。
                // アラームの時間が現在時刻よりも前であれば次の日の時刻とする
                // 6時から3時間おきに天気情報を取得する
                val cnt =
                    if (alarmTime.hour < currentTime.hour) {
                        (alarmTime.hour + 24 - FIRST_FORECAST_TIME) / 3 + 1
                    } else {
                        (alarmTime.hour - FIRST_FORECAST_TIME) / 3 + 1
                    }
                getWeatherByLocation(id, result.lat, result.lon, cnt)
            } catch (e: Exception) {
                updateAlarmUiState(id) {
                    it.copy(
                        coordinateState = CoordinateState.Error(e.message ?: "Unknown error"),
                    )
                }
            }
        }
    }

    private fun getWeatherByLocation(
        id: Int,
        lat: Double,
        lon: Double,
        cnt: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            updateAlarmUiState(id) {
                it.copy(
                    weatherState = WeatherState.Loading,
                )
            }
            try {
                val result = getWeatherRepositoryImpl.getWeather(lat, lon, cnt)
                updateAlarmUiState(id) {
                    it.copy(
                        weatherState =
                            WeatherState.Success(
                                result.list
                                    .last()
                                    .weather[0]
                                    .description,
                            ),
                    )
                }
            } catch (e: Exception) {
                Log.d("result", e.message.toString())
                updateAlarmUiState(id) {
                    it.copy(
                        weatherState = WeatherState.Error(e.message ?: "Unknown error"),
                    )
                }
            }
        }
    }

    private fun updateAlarmUiState(
        id: Int,
        updateAlarmUiState: (AlarmUiState) -> AlarmUiState,
    ) {
        _homeUiState.update {
            it.copy(
                alarmItemList =
                    it.alarmItemList.map { alarmUiState ->
                        if (alarmUiState.alarmItemState.id == id) {
                            updateAlarmUiState(alarmUiState)
                        } else {
                            alarmUiState
                        }
                    },
            )
        }
    }
}
