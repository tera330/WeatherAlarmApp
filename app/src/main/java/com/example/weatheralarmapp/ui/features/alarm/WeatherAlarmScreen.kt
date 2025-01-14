package com.example.weatheralarmapp.ui.features.alarm

import android.app.AlarmManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.ui.features.alarm.components.ToggleTimePicker
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlarmScreen(
    modifier: Modifier,
    alarmViewModel: AlarmViewModel = hiltViewModel(),
    showTimePicker: Boolean,
    onShowTimePickerChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(AlarmManager::class.java) as AlarmManager
    val isBadWeather = remember { mutableStateOf(false) }

    val alarmUiState = alarmViewModel.alarmUiState.collectAsState().value
    val alarmItemState = alarmUiState.alarmItemState

    val homeUiState by alarmViewModel.homeUiState.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        if (homeUiState.alarmItemList.isEmpty()) {
            Text(
                text = stringResource(R.string.nothing_alarm),
                fontSize = 20.sp,
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(items = homeUiState.alarmItemList, key = { it.alarmItemState.id }) { item ->
                    Column {
                        AlarmItem(
                            modifier = Modifier,
                            alarmUiState = item,
                            // expandedAlarmItem = { alarmViewModel.expandedAlarmItem() },
                            updateUntilTime = { id, hours, minutes ->
                                alarmViewModel.updateUntilAlarmTime(id, hours, minutes)
                            },
                            updateUntilAlarmTimeByWeather = { id, hours, minutes ->
                                alarmViewModel.updateUntilAlarmTimeByWeather(id, hours, minutes)
                            },
                            onSwitchAlarm = { Boolean ->
                                alarmViewModel.updateAlarmItem(
                                    alarmManager,
                                    com.example.weatheralarmapp.data.local.AlarmItem(
                                        id = item.alarmItemState.id,
                                        alarmTime = item.alarmItemState.alarmTime,
                                        selectedEarlyAlarmTime = item.alarmItemState.selectedEarlyAlarmTime,
                                        changedAlarmTImeByWeather = item.alarmItemState.changedAlarmTImeByWeather,
                                        isAlarmOn = Boolean,
                                        isWeatherForecastOn = item.alarmItemState.isWeatherForecastOn,
                                    ),
                                    isBadWeather.value,
                                )
                            },
                            onSwitchWeatherForecast = { Boolean ->
                                alarmViewModel.updateAlarmItem(
                                    alarmManager,
                                    com.example.weatheralarmapp.data.local.AlarmItem(
                                        id = item.alarmItemState.id,
                                        alarmTime = item.alarmItemState.alarmTime,
                                        selectedEarlyAlarmTime = item.alarmItemState.selectedEarlyAlarmTime,
                                        changedAlarmTImeByWeather = item.alarmItemState.changedAlarmTImeByWeather,
                                        isAlarmOn = item.alarmItemState.isAlarmOn,
                                        isWeatherForecastOn = Boolean,
                                    ),
                                    isBadWeather.value,
                                )
                            },
                            isBadWeather = { Boolean ->
                                isBadWeather.value = Boolean
                            },
                            selectTime = { String ->
                                alarmViewModel.updateAlarmItem(
                                    alarmManager,
                                    com.example.weatheralarmapp.data.local.AlarmItem(
                                        id = item.alarmItemState.id,
                                        alarmTime = String,
                                        selectedEarlyAlarmTime = item.alarmItemState.selectedEarlyAlarmTime,
                                        changedAlarmTImeByWeather = String,
                                        isAlarmOn = item.alarmItemState.isAlarmOn,
                                        isWeatherForecastOn = item.alarmItemState.isWeatherForecastOn,
                                    ),
                                    isBadWeather.value,
                                )
                            },
                            selectRadioButton = { String ->
                                val baseTime = LocalTime.parse(item.alarmItemState.alarmTime)
                                val changedAlarmTImeByWeather =
                                    when (String) {
                                        "00:00" -> item.alarmItemState.alarmTime
                                        "00:15" -> baseTime.minusMinutes(15).toString()
                                        "00:30" -> baseTime.minusMinutes(30).toString()
                                        "00:45" -> baseTime.minusMinutes(45).toString()
                                        "00:60" -> baseTime.minusHours(1).toString()
                                        else -> item.alarmItemState.alarmTime
                                    }
                                alarmViewModel.updateAlarmItem(
                                    alarmManager,
                                    com.example.weatheralarmapp.data.local.AlarmItem(
                                        id = item.alarmItemState.id,
                                        alarmTime = item.alarmItemState.alarmTime,
                                        selectedEarlyAlarmTime = String,
                                        changedAlarmTImeByWeather = changedAlarmTImeByWeather,
                                        isAlarmOn = item.alarmItemState.isAlarmOn,
                                        isWeatherForecastOn = item.alarmItemState.isWeatherForecastOn,
                                    ),
                                    isBadWeather.value,
                                )
                            },
                            onDeleteAlarm = {
                                alarmViewModel.deleteAlarmItem(item, alarmManager)
                            },
                            fetchWeather = {
                                alarmViewModel.getWeatherByCityName(
                                    item.alarmItemState.id,
                                    "Tokyo",
                                    LocalTime.parse(item.alarmItemState.alarmTime),
                                )
                            },
                            alarmManager = alarmManager,
                        )
                    }
                }
            }
        }
        if (showTimePicker) {
            ToggleTimePicker(
                onConfirm = { timePicker ->
                    val hourStr: String =
                        if (timePicker.hour < 10) {
                            "0${timePicker.hour}"
                        } else {
                            timePicker.hour.toString()
                        }

                    val minuteStr: String =
                        if (timePicker.minute < 10) {
                            if (timePicker.minute == 0) "00" else "0${timePicker.minute}"
                        } else {
                            timePicker.minute.toString()
                        }

                    alarmViewModel.addAlarmItem(
                        alarmManager = alarmManager,
                        com.example.weatheralarmapp.data.local.AlarmItem(
                            id = alarmItemState.id,
                            alarmTime = "$hourStr:$minuteStr",
                            selectedEarlyAlarmTime = alarmItemState.selectedEarlyAlarmTime,
                            changedAlarmTImeByWeather = "$hourStr:$minuteStr",
                            isAlarmOn = alarmItemState.isAlarmOn,
                            isWeatherForecastOn = alarmItemState.isWeatherForecastOn,
                        ),
                    )

                    onShowTimePickerChange(false)
                },
                onDismiss = { onShowTimePickerChange(false) },
                alarmUiState = alarmUiState,
            )
        }
    }
}

@Preview
@Composable
fun WeatherAlarmScreenPreview() {
    WeatherAlarmScreen(
        modifier = Modifier.fillMaxSize(),
        showTimePicker = false,
        onShowTimePickerChange = { Boolean -> },
    )
}
