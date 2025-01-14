package com.example.weatheralarmapp.data.repository

import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.local.AlarmItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlarmItemRepositoryImpl
    @Inject
    constructor(
        private val alarmItemDao: AlarmItemDao,
    ) : AlarmItemRepository {
        override fun getAllAlarmItemsStream(): Flow<List<AlarmItem>> = alarmItemDao.getAllAlarmItems()

        override suspend fun insertAlarmItem(alarmItem: AlarmItem) =
            withContext(Dispatchers.IO) {
                alarmItemDao.insert(alarmItem)
            }

        override suspend fun updateAlarmItem(alarmItem: AlarmItem) =
            withContext(Dispatchers.IO) {
                alarmItemDao.update(alarmItem)
            }

        override suspend fun deleteAlarmItem(alarmItem: AlarmItem) =
            withContext(Dispatchers.IO) {
                alarmItemDao.delete(alarmItem)
            }

        override suspend fun deleteAlarmItemById(alarmItem: AlarmItem) = alarmItemDao.delete(alarmItem)
    }
