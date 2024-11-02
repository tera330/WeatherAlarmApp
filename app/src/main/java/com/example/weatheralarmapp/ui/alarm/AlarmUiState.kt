package com.example.weatheralarmapp.ui.alarm

data class AlarmUiState(
    val id: Int,
    val alarmTime: String,
    val selectedEarlyAlarmTime: String,
    val changedAlarmTImeByWeather: String,
    val isAlarmOn: Boolean,
    val isWeatherForecastOn: Boolean,
)
