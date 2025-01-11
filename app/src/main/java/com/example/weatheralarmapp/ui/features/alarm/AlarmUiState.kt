package com.example.weatheralarmapp.ui.features.alarm

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

// 都市名から緯度経度を取得するAPIのレスポンス
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

data class AlarmUiState(
    val alarmItemState: AlarmItemState,
    val weatherState: WeatherState = WeatherState.Initial,
    val coordinateState: CoordinateState = CoordinateState.Initial,
    var expandedAlarmItem: Boolean = false,
    var hoursUntilAlarm: Long = 0L,
    var minutesUntilAlarm: Long = 0L,
    var earlyHoursUntilAlarmByWeather: Long = 10L,
    var earlyMinutesUntilAlarmByWeather: Long = 10L,
)
