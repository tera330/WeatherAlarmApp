package com.example.weatheralarmapp.alarm

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.di.UseCaseModule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class AlarmHelperTest {
    @Test
    fun addAlarmItemShouldSetAlarmUsingAlarmManager() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val alarmHelper = UseCaseModule.AlarmHelperModule.provideAlarmHelper(context)
            val alarmManager = context.getSystemService(AlarmManager::class.java) as AlarmManager

            val alarmItem =
                AlarmItem(
                    id = 1,
                    alarmTime = "07:00",
                    changedAlarmTImeByWeather = "06:30",
                    selectedEarlyAlarmTime = "00:30",
                    isAlarmOn = true,
                    isWeatherForecastOn = true,
                )

            val shadowAlarmManager = Shadows.shadowOf(alarmManager)

            alarmHelper.setAlarm(alarmManager, alarmItem, false)

            val scheduledAlarms = shadowAlarmManager.scheduledAlarms
            assertEquals(1, scheduledAlarms.size)

            val triggerTime = Date(scheduledAlarms[0].triggerAtMs)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            assertEquals("08:00", timeFormat.format(triggerTime))
        }
}
