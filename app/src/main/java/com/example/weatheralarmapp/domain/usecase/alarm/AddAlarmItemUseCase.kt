package com.example.weatheralarmapp.domain.usecase.alarm

import android.app.AlarmManager
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.domain.AlarmHelper
import javax.inject.Inject

class AddAlarmItemUseCase
    @Inject
    constructor(
        private val alarmHelper: AlarmHelper,
        private val alarmItemRepository: AlarmItemRepository,
    ) {
        suspend fun addAlarmItem(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
        ) {
            alarmItemRepository.insertAlarmItem(alarmItem)
            alarmHelper.setAlarm(alarmManager, alarmItem, false)
        }
    }
