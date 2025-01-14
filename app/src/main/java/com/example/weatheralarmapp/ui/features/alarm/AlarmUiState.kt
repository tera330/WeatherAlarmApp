package com.example.weatheralarmapp.ui.features.alarm

import com.example.weatheralarmapp.data.local.AlarmItem

// Roomが保持するデータ
data class AlarmItemState(
    val id: Int = 0,
    val alarmTime: String,
    val selectedEarlyAlarmTime: String = "00:00",
    val changedAlarmTImeByWeather: String,
    val isAlarmOn: Boolean = true,
    val isWeatherForecastOn: Boolean = false,
)

// 天気取得APIのレスポンス
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

data class AlarmUiState(
    val alarmItemState: AlarmItemState,
    val weatherState: WeatherState = WeatherState.Initial,
    var expandedAlarmItem: Boolean = false,
    var hoursUntilAlarm: Long = 0L,
    var minutesUntilAlarm: Long = 0L,
    var earlyHoursUntilAlarmByWeather: Long = 10L,
    var earlyMinutesUntilAlarmByWeather: Long = 10L,
)

fun AlarmItemState.toAlarmItem(alarmItemState: AlarmItemState): AlarmItem =
    AlarmItem(
        id = alarmItemState.id,
        alarmTime = alarmItemState.alarmTime,
        changedAlarmTImeByWeather = alarmItemState.changedAlarmTImeByWeather,
        selectedEarlyAlarmTime = alarmItemState.selectedEarlyAlarmTime,
        isAlarmOn = alarmItemState.isAlarmOn,
        isWeatherForecastOn = alarmItemState.isWeatherForecastOn,
    )
