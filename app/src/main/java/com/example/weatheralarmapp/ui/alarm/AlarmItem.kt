package com.example.weatheralarmapp.ui.alarm

import android.app.AlarmManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.WeatherState
import com.example.weatheralarmapp.dateformat.createHourString
import com.example.weatheralarmapp.dateformat.createMinuteString
import com.example.weatheralarmapp.ui.common.ExpandButton
import java.time.Duration
import java.time.LocalTime
import kotlin.concurrent.timer

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(
    modifier: Modifier,
    alarmUiState: AlarmUiState,
    weatherState: WeatherState,
    onSwitchAlarm: (Boolean) -> Unit,
    onSwitchWeatherForecast: (Boolean) -> Unit,
    selectTime: (String) -> Unit,
    onDeleteAlarm: () -> Unit,
    fetchWeather: () -> Unit,
    alarmManager: AlarmManager,
) {
    var expanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var hour by remember { mutableLongStateOf(0L) }
    var minute by remember { mutableLongStateOf(0L) }
    val openDialog =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            remember { mutableStateOf(false) }
        } else {
            remember { mutableStateOf(true) }
        }
    var secondsUntilNextMinute by remember { mutableIntStateOf(0) }
    var duration by remember {
        mutableStateOf(Duration.ZERO)
    }
    val radioOptions = listOf("15", "30", "45", "60")
    val selectedOption = remember { mutableStateOf("15") }

    Card(modifier = modifier.padding(5.dp)) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .animateContentSize(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                    ),
        ) {
            Row(modifier = modifier.fillMaxWidth()) {
                Text(
                    modifier =
                        modifier.clickable {
                            showTimePicker = !showTimePicker
                        },
                    text = alarmUiState.alarmTime,
                    fontSize = 40.sp,
                )
                Spacer(modifier = modifier.weight(1f))
                ExpandButton(
                    modifier = modifier,
                    expanded = expanded,
                    onClick = { expanded = !expanded },
                )
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val alarmText =
                    if (alarmUiState.isAlarmOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        var currentTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
                        val currentSecond = LocalTime.now().second
                        secondsUntilNextMinute = 60 - currentSecond

                        timer("updateCurrentTimeTimer", period = 60 * 1000L, initialDelay = secondsUntilNextMinute * 1000L) {
                            currentTime = LocalTime.now().withSecond(0).withNano(0)
                        }

                        val alarmTime =
                            if (alarmUiState.alarmTime.length == 5) {
                                LocalTime.parse(alarmUiState.alarmTime)
                            } else {
                                LocalTime.parse("0${alarmUiState.alarmTime}")
                            }

                        LaunchedEffect(Unit) {
                            fetchWeather()
                        }

                        // アラームの設定時間と現在時刻の差分を計算
                        LaunchedEffect(currentTime, alarmUiState.alarmTime, selectedOption.value, weatherState) {
                            duration =
                                if (alarmTime.isAfter(currentTime) || alarmTime == currentTime) {
                                    Duration.between(currentTime, alarmTime)
                                } else {
                                    val durationUntilMidnight =
                                        Duration.between(currentTime, LocalTime.MIDNIGHT)
                                    val durationAfterMidnight =
                                        Duration.between(LocalTime.MIDNIGHT, alarmTime)
                                    durationUntilMidnight.plus(
                                        durationAfterMidnight +
                                            Duration.ofDays(
                                                1,
                                            ),
                                    )
                                }
                            if (alarmUiState.isWeatherForecastOn) {
                                if (weatherState is WeatherState.Success) {
                                    when (weatherState.weather) {
                                        "曇りがち" -> {
                                            duration = duration.minus(Duration.ofMinutes(selectedOption.value.toLong()))
                                        }

                                        "適度な雨" -> {
                                            duration = duration.minus(Duration.ofMinutes(selectedOption.value.toLong()))
                                        }

                                        "雪" -> {
                                            duration = duration.minus(Duration.ofMinutes(selectedOption.value.toLong()))
                                        }
                                        else -> {}
                                    }
                                }
                            }

                            if (duration.toMinutes() >= 60) {
                                hour = duration.toMinutes() / 60
                                minute = duration.toMinutes() % 60
                            } else {
                                hour = 0
                                minute = duration.toMinutes()
                            }
                        }
                        stringResource(R.string.timeUntilAlarm, hour, minute)
                    } else {
                        stringResource(R.string.alarm_of_message)
                    }

                TextSwitchRow(
                    modifier = modifier,
                    text = alarmText,
                    isChecked = alarmUiState.isAlarmOn,
                    onSwitch = { onSwitchAlarm(it) },
                )
            }

            TextSwitchRow(
                modifier = modifier,
                text = "雨雪時に、選択した時間分アラームを早める",
                isChecked = alarmUiState.isWeatherForecastOn,
                onSwitch = {
                    onSwitchWeatherForecast(it)
                    // TODO 天気取得メソッドの呼び出し
                    fetchWeather()
                },
            )

            RequestExactAlarmPermission(openDialog = openDialog)

            if (expanded) {
                Column(modifier = modifier) {
                    RadioButtonGroup(
                        modifier = modifier,
                        radioOptions = radioOptions,
                        selectedOption = selectedOption.value,
                        onOptionSelected = { String ->
                            selectedOption.value = String
                        },
                    )

                    IconButton(onClick = {
                        onDeleteAlarm()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
    if (showTimePicker) {
        ToggleTimePicker(
            onConfirm = { timePicker ->
                val hourStr: String = createHourString(timePicker.hour)
                val minuteStr: String = createMinuteString(timePicker.minute)
                selectTime("$hourStr:$minuteStr")
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            alarmUiState = alarmUiState,
        )
    }
}

@Composable
fun RadioButtonGroup(
    modifier: Modifier,
    radioOptions: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    Row(modifier = modifier) {
        radioOptions.forEach { text ->
            Row(modifier) {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    RadioButton(
                        selected = text == selectedOption,
                        onClick = {
                            onOptionSelected(text)
                        },
                    )
                    Text(
                        text = "${text}分",
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RadioButtonGroupPreview() {
    val selectedOption = remember { mutableStateOf("15") }
    RadioButtonGroup(
        modifier = Modifier,
        radioOptions = listOf("15", "30", "45", "60"),
        selectedOption = selectedOption.value,
        onOptionSelected = { String -> selectedOption.value = String },
    )
}

@Composable
fun TextSwitchRow(
    modifier: Modifier,
    text: String,
    isChecked: Boolean,
    onSwitch: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text)
        Spacer(modifier = modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = {
                onSwitch(!isChecked)
            },
        )
    }
}

@Preview
@Composable
fun TextSwitchRowPreview() {
    TextSwitchRow(
        modifier = Modifier,
        text = "天気予報機能",
        isChecked = false,
        onSwitch = { Boolean -> },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun AlarmItemPreview() {
    val context = LocalContext.current
    val alarmManager = remember { context.applicationContext.getSystemService(AlarmManager::class.java) as AlarmManager }

    AlarmItem(
        modifier = Modifier,
        alarmUiState = AlarmUiState(0, "", false, false),
        weatherState = WeatherState.Initial,
        onSwitchAlarm = { Boolean -> },
        onSwitchWeatherForecast = { Boolean -> },
        selectTime = { String -> },
        onDeleteAlarm = { },
        fetchWeather = { },
        alarmManager = alarmManager,
    )
}
