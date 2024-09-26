package com.example.weatheralarmapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatheralarmapp.flux.FluxViewModel
import com.example.weatheralarmapp.flux.alarm.SetAlarmActionCreator
import com.example.weatheralarmapp.flux.common.Dispatcher
import com.example.weatheralarmapp.ui.alarm.AlarmItem
import com.example.weatheralarmapp.ui.theme.WeatherAlarmAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dispatcher: Dispatcher

    @Inject lateinit var alarmActionCreator: SetAlarmActionCreator

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAlarmAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherAlarmApp(
                        modifier = Modifier.padding(innerPadding),
                        dispatcher = dispatcher,
                        setAlarmActionCreator = alarmActionCreator,
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherAlarmApp(
    modifier: Modifier,
    dispatcher: Dispatcher,
    setAlarmActionCreator: SetAlarmActionCreator,
    fluxViewModel: FluxViewModel =
        viewModel {
            FluxViewModel(dispatcher)
        },
) {
    val alarmStore = fluxViewModel.alarmStore
    val alarmUiState = alarmStore.uiState.value

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn {
            items(1) {
                AlarmItem(
                    modifier = Modifier,
                    alarmUiState = alarmUiState,
                    onChangeAlarm = { Boolean -> setAlarmActionCreator.setAlarmOn(Boolean) },
                    selectTime = { String -> setAlarmActionCreator.setAlarmTime(String) },
                )
            }
        }
    }
}

@Preview
@Composable
fun WeatherAlarmAppPreview() {
    WeatherAlarmApp(
        modifier = Modifier.fillMaxSize(),
        dispatcher = Dispatcher(),
        setAlarmActionCreator = SetAlarmActionCreator(Dispatcher()),
    )
}
