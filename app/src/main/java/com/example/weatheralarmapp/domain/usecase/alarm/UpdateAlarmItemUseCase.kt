package com.example.weatheralarmapp.domain.usecase.alarm

import android.app.AlarmManager
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.domain.AlarmHelper
import javax.inject.Inject

class UpdateAlarmItemUseCase
    @Inject
    constructor(
        private val alarmHelper: AlarmHelper,
        private val alarmItemRepository: AlarmItemRepository,
    ) {
        suspend fun updateAlarmItem(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
            isBadWeather: Boolean,
        ) {
            alarmItemRepository.updateAlarmItem(alarmItem)

            // アラームをオンにした時
            if (alarmItem.isAlarmOn && !alarmItem.isWeatherForecastOn) {
                val changedAlarmByWeather = alarmItem.alarmTime
                alarmHelper.setAlarm(
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
                alarmHelper.setAlarm(
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
                alarmHelper.cancelAlarm(
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
    }
