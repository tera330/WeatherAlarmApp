package com.example.weatheralarmapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.weatheralarmapp.ui.alarm.AlarmItem
import com.example.weatheralarmapp.ui.alarm.AlarmUiState
import com.example.weatheralarmapp.ui.alarm.ToggleTimePicker
import com.example.weatheralarmapp.ui.home.HomeUiState
import com.example.weatheralarmapp.ui.theme.WeatherAlarmAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var alarmItemRepository: AlarmItemRepository

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
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlarmApp(
    modifier: Modifier,
    alarmItemRepository: AlarmItemRepository,
    alarmViewModel: AlarmViewModel =
        viewModel {
            AlarmViewModel(alarmItemRepository)
        },
    showTimePicker: Boolean,
    onShowTimePickerChange: (Boolean) -> Unit,
    alarmUiState: AlarmUiState = alarmViewModel.alarmUiState.value,
    homeUiState: State<HomeUiState> = alarmViewModel.homeUiState.collectAsState(),
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (homeUiState.value.alarmItemList.isEmpty()) {
            Text(
                text = "アラームがありません",
                fontSize = 20.sp,
            )
        } else {
            LazyColumn {
                items(items = homeUiState.value.alarmItemList, key = { it.id }) { item ->
                    Column {
                        AlarmItem(
                            modifier = Modifier,
                            alarmUiState =
                                AlarmUiState(
                                    id = item.id,
                                    isAlarmOn = item.isAlarmOn,
                                    alarmTime = item.alarmTime,
                                ),
                            onSwitchAlarm = { Boolean ->
                                scope.launch {
                                    alarmViewModel.updateAlarmItem(
                                        AlarmItem(
                                            id = item.id,
                                            alarmTime = item.alarmTime,
                                            isAlarmOn = Boolean,
                                        ),
                                    )
                                }
                            },
                            selectTime = { String ->
                                scope.launch {
                                    alarmViewModel.updateAlarmItem(
                                        AlarmItem(
                                            id = item.id,
                                            alarmTime = String,
                                            isAlarmOn = item.isAlarmOn,
                                        ),
                                    )
                                }
                            },
                            onDeleteAlarm = {
                                scope.launch {
                                    alarmViewModel.deleteAlarmItem(item)
                                }
                            },
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
                            AlarmItem(
                                id = alarmUiState.id,
                                alarmTime = "$hourStr:$minuteStr",
                                isAlarmOn = alarmUiState.isAlarmOn,
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
fun WeatherAlarmAppPreview() {
    val context = LocalContext.current
    val alarmItemRepository: AlarmItemRepository by lazy {
        AlarmItemRepositoryImpl(AlarmDatabase.getDatabase(context).alarmItemDao())
    }

    WeatherAlarmApp(
        modifier = Modifier.fillMaxSize(),
        alarmItemRepository = alarmItemRepository,
        showTimePicker = false,
        onShowTimePickerChange = { Boolean -> },
    )
}
