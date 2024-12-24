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

data class AlarmUiState(
    val alarmItemState: AlarmItemState,
    var expandedAlarmItem: Boolean = false,
    var hoursUntilAlarm: Long = 0L,
    var minutesUntilAlarm: Long = 0L,
)
