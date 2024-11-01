package com.example.weatheralarmapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_items")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val alarmTime: String,
    val isAlarmOn: Boolean,
    val isWeatherForecastOn: Boolean,
)
