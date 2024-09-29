package com.example.weatheralarmapp.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmItemRepositoryImpl
    @Inject
    constructor(
        private val alarmItemDao: AlarmItemDao,
    ) : AlarmItemRepository {
        override fun getAllAlarmItemsStream(): Flow<List<AlarmItem>> = alarmItemDao.getAllAlarmItems()

        override suspend fun insertAlarmItem(alarmItem: AlarmItem) = alarmItemDao.insert(alarmItem)

        override suspend fun updateAlarmItem(alarmItem: AlarmItem) = alarmItemDao.update(alarmItem)

        override suspend fun deleteAlarmItem(alarmItem: AlarmItem) = alarmItemDao.delete(alarmItem)

        override suspend fun deleteAlarmItemById(alarmItem: AlarmItem) = alarmItemDao.delete(alarmItem)
    }
