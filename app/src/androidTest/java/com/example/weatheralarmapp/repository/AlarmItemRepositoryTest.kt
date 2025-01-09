package com.example.weatheralarmapp.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weatheralarmapp.data.local.AlarmDatabase
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.local.AlarmItemDao
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.data.repository.AlarmItemRepositoryImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.jvm.Throws

class AlarmItemRepositoryTest {
    private lateinit var alarmItemRepository: AlarmItemRepository
    private lateinit var alarmItemDao: AlarmItemDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db =
            Room
                .inMemoryDatabaseBuilder(context, AlarmDatabase::class.java)
                .build()
        alarmItemDao = db.alarmItemDao()
        alarmItemRepository = AlarmItemRepositoryImpl(alarmItemDao)
    }

    @Test
    @Throws(Exception::class)
    fun addAlarmItemShouldInsertDataIntoRoomDB(): Unit =
        runTest {
            val alarmItem =
                AlarmItem(
                    id = 1,
                    alarmTime = "07:00",
                    changedAlarmTImeByWeather = "06:30",
                    selectedEarlyAlarmTime = "00:30",
                    isAlarmOn = true,
                    isWeatherForecastOn = true,
                )

            alarmItemRepository.insertAlarmItem(alarmItem)

            // データベースにデータが正しく挿入されていることを確認
            val insertedItem = alarmItemDao.getAlarmItemById(1)
            assertNotNull(insertedItem)
            assertEquals(alarmItem, insertedItem)
        }
}
