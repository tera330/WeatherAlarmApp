package com.example.weatheralarmapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAlarmAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "アラーム",
                                    fontSize = 25.sp,
                                )
                            },
                        )
                    },
                    floatingActionButton = {
                        LargeFloatingActionButton(
                            onClick = { /*TODO*/ },
                            shape = CircleShape,
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                ) { innerPadding ->
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(10.dp),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            items(10) {
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
