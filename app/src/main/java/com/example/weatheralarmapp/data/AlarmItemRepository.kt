package com.example.weatheralarmapp.data

import kotlinx.coroutines.flow.Flow

interface AlarmItemRepository {
    fun getAllAlarmItemsStream(): Flow<List<AlarmItem>>

    suspend fun insertAlarmItem(alarmItem: AlarmItem)

    suspend fun updateAlarmItem(alarmItem: AlarmItem)

    suspend fun deleteAlarmItem(alarmItem: AlarmItem)

    suspend fun deleteAlarmItemById(alarmItem: AlarmItem)
}
