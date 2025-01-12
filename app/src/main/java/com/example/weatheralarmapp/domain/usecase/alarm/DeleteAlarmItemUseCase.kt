package com.example.weatheralarmapp.domain.usecase.alarm

import android.app.AlarmManager
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.domain.AlarmHelper
import javax.inject.Inject

class DeleteAlarmItemUseCase
    @Inject
    constructor(
        private val alarmHelper: AlarmHelper,
        private val alarmItemRepository: AlarmItemRepository,
    ) {
        suspend fun deleteAlarmItem(
            alarmItem: AlarmItem,
            alarmManager: AlarmManager,
        ) {
            alarmHelper.cancelAlarm(
                alarmManager,
                alarmItem,
            )
            alarmItemRepository.deleteAlarmItem(alarmItem)
        }
    }
