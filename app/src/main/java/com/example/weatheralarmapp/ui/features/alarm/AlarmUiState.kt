package com.example.weatheralarmapp.ui.features.alarm

// Roomが保持するデータ
data class AlarmItemState(
    val id: Int,
    val alarmTime: String,
    val selectedEarlyAlarmTime: String,
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
    val weatherState: WeatherState,
    var expandedAlarmItem: Boolean = false,
    var hoursUntilAlarm: Long = 0L,
    var minutesUntilAlarm: Long = 0L,
)
