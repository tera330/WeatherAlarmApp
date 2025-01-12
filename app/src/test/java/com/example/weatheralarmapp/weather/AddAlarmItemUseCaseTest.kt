package com.example.weatheralarmapp.weather

import android.app.AlarmManager
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.domain.AlarmHelper
import com.example.weatheralarmapp.domain.usecase.alarm.AddAlarmItemUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddAlarmItemUseCaseTest {
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var alarmItemRepository: AlarmItemRepository
    private lateinit var addAlarmItemUseCase: AddAlarmItemUseCase
    private lateinit var alarmManager: AlarmManager

    @Before
    fun setUp() {
        alarmHelper = mockk()
        alarmItemRepository = mockk()
        alarmManager = mockk()
        addAlarmItemUseCase = AddAlarmItemUseCase(alarmHelper, alarmItemRepository)
    }

    @Test
    fun `addAlarmItem should call insertAlarmItem and setAlarm exactly once`() =
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

            coEvery { alarmItemRepository.insertAlarmItem(alarmItem) } just Runs
            every { alarmHelper.setAlarm(alarmManager, alarmItem, false) } just Runs

            addAlarmItemUseCase.addAlarmItem(alarmManager, alarmItem)

            coVerify(exactly = 1) { alarmItemRepository.insertAlarmItem(alarmItem) }
            verify(exactly = 1) { alarmHelper.setAlarm(alarmManager, alarmItem, false) }
        }
}
