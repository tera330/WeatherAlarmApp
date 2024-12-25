package com.example.weatheralarmapp.ui.features.alarm

import android.app.AlarmManager
import android.app.Application
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.data.local.AlarmDatabase
import com.example.weatheralarmapp.data.remote.WeatherApi
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.data.repository.AlarmItemRepositoryImpl
import com.example.weatheralarmapp.data.repository.GetWeatherRepositoryImpl
import com.example.weatheralarmapp.ui.features.alarm.components.ToggleTimePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlarmScreen(
    modifier: Modifier,
    context: Application = LocalContext.current.applicationContext as Application,
    alarmItemRepository: AlarmItemRepository,
    getWeatherRepository: GetWeatherRepositoryImpl,
    alarmViewModel: AlarmViewModel =
        viewModel {
            AlarmViewModel(
                context.applicationContext as Application,
                alarmItemRepository,
                getWeatherRepository,
            )
        },
    showTimePicker: Boolean,
    onShowTimePickerChange: (Boolean) -> Unit,
) {
    val alarmManager = context.getSystemService(AlarmManager::class.java) as AlarmManager
    val scope = rememberCoroutineScope()
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
                            expandedAlarmItem = { alarmViewModel.expandedAlarmItem() },
                            updateUntilTime = { id, hours, minutes ->
                                alarmViewModel.updateUntilAlarmTime(id, hours, minutes)
                            },
                            updateUntilAlarmTimeByWeather = { id, hours, minutes ->
                                alarmViewModel.updateUntilAlarmTimeByWeather(id, hours, minutes)
                            },
                            onSwitchAlarm = { Boolean ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
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
                                    }
                                }
                            },
                            onSwitchWeatherForecast = { Boolean ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
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
                                    }
                                }
                            },
                            isBadWeather = { Boolean ->
                                isBadWeather.value = Boolean
                            },
                            selectTime = { String ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
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
                                    }
                                }
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
                                scope.launch {
                                    withContext(Dispatchers.IO) {
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
                                    }
                                }
                            },
                            onDeleteAlarm = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        alarmViewModel.deleteAlarmItem(item, alarmManager)
                                    }
                                }
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

                    scope.launch {
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
                    }
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
    val context = LocalContext.current
    val alarmItemRepository: AlarmItemRepository by lazy {
        AlarmItemRepositoryImpl(AlarmDatabase.getDatabase(context).alarmItemDao())
    }
    val weatherApiService = WeatherApi.retrofitService

    WeatherAlarmScreen(
        modifier = Modifier.fillMaxSize(),
        alarmItemRepository = alarmItemRepository,
        getWeatherRepository = GetWeatherRepositoryImpl(weatherApiService),
        showTimePicker = false,
        onShowTimePickerChange = { Boolean -> },
    )
}
