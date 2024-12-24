package com.example.weatheralarmapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.data.repository.GetWeatherRepositoryImpl
import com.example.weatheralarmapp.ui.features.alarm.WeatherAlarmScreen
import com.example.weatheralarmapp.ui.theme.WeatherAlarmAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var alarmItemRepository: AlarmItemRepository

    @Inject lateinit var getWeatherRepository: GetWeatherRepositoryImpl

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
                    WeatherAlarmScreen(
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
