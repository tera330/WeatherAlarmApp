package com.example.weatheralarmapp

import android.app.AlarmManager
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatheralarmapp.data.AlarmDatabase
import com.example.weatheralarmapp.data.AlarmItem
import com.example.weatheralarmapp.data.AlarmItemRepository
import com.example.weatheralarmapp.data.AlarmItemRepositoryImpl
import com.example.weatheralarmapp.data.GetWeatherRepositoryImpl
import com.example.weatheralarmapp.network.WeatherApi
import com.example.weatheralarmapp.ui.alarm.AlarmItem
import com.example.weatheralarmapp.ui.alarm.AlarmUiState
import com.example.weatheralarmapp.ui.alarm.ToggleTimePicker
import com.example.weatheralarmapp.ui.home.HomeUiState
import com.example.weatheralarmapp.ui.theme.WeatherAlarmAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var alarmItemRepository: AlarmItemRepository

    @Inject lateinit var getWeatherRepository: GetWeatherRepositoryImpl

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAlarmAppTheme {
                var showTimePicker by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.alarm_title),
                                    fontSize = 25.sp,
                                )
                            },
                        )
                    },
                    floatingActionButton = {
                        LargeFloatingActionButton(
                            onClick = { showTimePicker = !showTimePicker },
                            shape = CircleShape,
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                ) { innerPadding ->
                    WeatherAlarmApp(
                        modifier = Modifier.padding(innerPadding),
                        showTimePicker = showTimePicker,
                        onShowTimePickerChange = { Boolean ->
                            showTimePicker = Boolean
                        },
                        alarmItemRepository = alarmItemRepository,
                        getWeatherRepository = getWeatherRepository,
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlarmApp(
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
    alarmUiState: AlarmUiState = alarmViewModel.alarmUiState.value,
    homeUiState: State<HomeUiState> = alarmViewModel.homeUiState.collectAsState(),
) {
    val alarmManager = context.getSystemService(AlarmManager::class.java) as AlarmManager
    val scope = rememberCoroutineScope()
    val isBadWeather = remember { mutableStateOf(false) }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        if (homeUiState.value.alarmItemList.isEmpty()) {
            Text(
                text = stringResource(R.string.nothing_alarm),
                fontSize = 20.sp,
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                items(items = homeUiState.value.alarmItemList, key = { it.id }) { item ->
                    Column {
                        AlarmItem(
                            modifier = Modifier,
                            alarmUiState =
                                AlarmUiState(
                                    id = item.id,
                                    isAlarmOn = item.isAlarmOn,
                                    selectedEarlyAlarmTime = item.selectedEarlyAlarmTime,
                                    isWeatherForecastOn = item.isWeatherForecastOn,
                                    alarmTime = item.alarmTime,
                                    changedAlarmTImeByWeather = item.changedAlarmTImeByWeather,
                                ),
                            weatherState = alarmViewModel.weatherState.value,
                            onSwitchAlarm = { Boolean ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        alarmViewModel.updateAlarmItem(
                                            alarmManager,
                                            AlarmItem(
                                                id = item.id,
                                                alarmTime = item.alarmTime,
                                                selectedEarlyAlarmTime = item.selectedEarlyAlarmTime,
                                                changedAlarmTImeByWeather = item.changedAlarmTImeByWeather,
                                                isAlarmOn = Boolean,
                                                isWeatherForecastOn = item.isWeatherForecastOn,
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
                                            AlarmItem(
                                                id = item.id,
                                                alarmTime = item.alarmTime,
                                                selectedEarlyAlarmTime = item.selectedEarlyAlarmTime,
                                                changedAlarmTImeByWeather = item.changedAlarmTImeByWeather,
                                                isAlarmOn = item.isAlarmOn,
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
                                            AlarmItem(
                                                id = item.id,
                                                alarmTime = String,
                                                selectedEarlyAlarmTime = item.selectedEarlyAlarmTime,
                                                changedAlarmTImeByWeather = String,
                                                isAlarmOn = item.isAlarmOn,
                                                isWeatherForecastOn = item.isWeatherForecastOn,
                                            ),
                                            isBadWeather.value,
                                        )
                                    }
                                }
                            },
                            selectRadioButton = { String ->
                                val baseTime = LocalTime.parse(item.alarmTime)
                                val changedAlarmTImeByWeather =
                                    when (String) {
                                        "00:00" -> item.alarmTime
                                        "00:15" -> baseTime.minusMinutes(15).toString()
                                        "00:30" -> baseTime.minusMinutes(30).toString()
                                        "00:45" -> baseTime.minusMinutes(45).toString()
                                        "00:60" -> baseTime.minusHours(1).toString()
                                        else -> item.alarmTime
                                    }
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        alarmViewModel.updateAlarmItem(
                                            alarmManager,
                                            AlarmItem(
                                                id = item.id,
                                                alarmTime = item.alarmTime,
                                                selectedEarlyAlarmTime = String,
                                                changedAlarmTImeByWeather = changedAlarmTImeByWeather,
                                                isAlarmOn = item.isAlarmOn,
                                                isWeatherForecastOn = item.isWeatherForecastOn,
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
                                alarmViewModel.getWeatherByCityName("Tokyo", LocalTime.parse(item.alarmTime))
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
                            AlarmItem(
                                id = alarmUiState.id,
                                alarmTime = "$hourStr:$minuteStr",
                                selectedEarlyAlarmTime = alarmUiState.selectedEarlyAlarmTime,
                                changedAlarmTImeByWeather = "$hourStr:$minuteStr",
                                isAlarmOn = alarmUiState.isAlarmOn,
                                isWeatherForecastOn = alarmUiState.isWeatherForecastOn,
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun WeatherAlarmAppPreview() {
    val context = LocalContext.current
    val alarmItemRepository: AlarmItemRepository by lazy {
        AlarmItemRepositoryImpl(AlarmDatabase.getDatabase(context).alarmItemDao())
    }
    val weatherApiService = WeatherApi.retrofitService

    WeatherAlarmApp(
        modifier = Modifier.fillMaxSize(),
        alarmItemRepository = alarmItemRepository,
        getWeatherRepository = GetWeatherRepositoryImpl(weatherApiService),
        showTimePicker = false,
        onShowTimePickerChange = { Boolean -> },
    )
}
