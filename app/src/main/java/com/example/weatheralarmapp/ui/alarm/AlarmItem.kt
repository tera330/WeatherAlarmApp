package com.example.weatheralarmapp.ui.alarm

import android.app.AlarmManager
import android.os.Build
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(
    modifier: Modifier,
    alarmUiState: AlarmUiState,
    weatherState: WeatherState,
    expandedAlarmItem: () -> Unit,
    updateUntilTime: (Long, Long) -> Unit,
    onSwitchAlarm: (Boolean) -> Unit,
    onSwitchWeatherForecast: (Boolean) -> Unit,
    selectTime: (String) -> Unit,
    selectRadioButton: (String) -> Unit,
    isBadWeather: (Boolean) -> Unit,
    onDeleteAlarm: () -> Unit,
    fetchWeather: () -> Unit,
    alarmManager: AlarmManager,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val alarmPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            remember { mutableStateOf(true) }
        } else {
            remember { mutableStateOf(false) }
        }
    val openAlarmPermissionDialog = remember { mutableStateOf(false) }
    var secondsUntilNextMinute by remember { mutableIntStateOf(0) }
    var duration by remember { mutableStateOf(Duration.ZERO) }
    val radioOptions = listOf("15", "30", "45", "60")
    val alarmItemState = alarmUiState.alarmItemState

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
            AlarmTimeRow(
                modifier = modifier,
                toggleShowTimePicker = { showTimePicker = !showTimePicker },
                alarmText = alarmItemState.alarmTime,
                changedAlarmText = alarmItemState.changedAlarmTImeByWeather,
                expanded = alarmUiState.expandedAlarmItem,
                onExpandToggle = { expandedAlarmItem() },
            )

            Spacer(modifier = Modifier.padding(5.dp))
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val alarmText =
                    if (alarmItemState.isAlarmOn) {
                        var currentTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
                        val currentSecond = LocalTime.now().second
                        secondsUntilNextMinute = 60 - currentSecond

                        timer("updateCurrentTimeTimer", period = 60 * 1000L, initialDelay = secondsUntilNextMinute * 1000L) {
                            currentTime = LocalTime.now().withSecond(0).withNano(0)
                        }

                        val alarmTime =
                            if (alarmItemState.alarmTime.length == 5) {
                                LocalTime.parse(alarmItemState.alarmTime)
                            } else {
                                LocalTime.parse("0${alarmItemState.alarmTime}")
                            }

                        LaunchedEffect(Unit) {
                            fetchWeather()
                        }

                        // アラームの設定時間と現在時刻の差分を計算
                        LaunchedEffect(currentTime, alarmItemState.alarmTime, alarmItemState.selectedEarlyAlarmTime, weatherState) {
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
                            if (alarmItemState.isWeatherForecastOn) {
                                if (weatherState is WeatherState.Success) {
                                    when (weatherState.weather) {
                                        "小雨" -> {
                                            if (duration.toMinutes() >= alarmItemState.selectedEarlyAlarmTime.toLong()) {
                                                duration =
                                                    duration.minus(Duration.ofMinutes(alarmItemState.selectedEarlyAlarmTime.toLong()))
                                            }
                                            isBadWeather(true)
                                        }

                                        "適度な雨" -> {
                                            if (duration.toMinutes() >= alarmItemState.selectedEarlyAlarmTime.toLong()) {
                                                duration =
                                                    duration.minus(Duration.ofMinutes(alarmItemState.selectedEarlyAlarmTime.toLong()))
                                            }
                                            isBadWeather(true)
                                        }

                                        "雪" -> {
                                            if (duration.toMinutes() >= alarmItemState.selectedEarlyAlarmTime.toLong()) {
                                                duration =
                                                    duration.minus(Duration.ofMinutes(alarmItemState.selectedEarlyAlarmTime.toLong()))
                                            }
                                            isBadWeather(true)
                                        }
                                        else -> {
                                            isBadWeather(false)
                                        }
                                    }
                                }
                            }

                            if (duration.toMinutes() >= 60) {
                                val hour = duration.toMinutes() / 60
                                val minute = duration.toMinutes() % 60

                                updateUntilTime(hour, minute)
                            } else {
                                val hour = 0.toLong()
                                val minute = duration.toMinutes()

                                updateUntilTime(hour, minute)
                            }
                        }
                        stringResource(R.string.timeUntilAlarm, alarmUiState.hoursUntilAlarm, alarmUiState.minutesUntilAlarm)
                    } else {
                        stringResource(R.string.alarm_of_message)
                    }

                TextSwitchRow(
                    modifier = modifier,
                    text = alarmText,
                    isChecked = alarmItemState.isAlarmOn,
                    onSwitch = { isChecked ->
                        if (!alarmPermission.value && isChecked) {
                            openAlarmPermissionDialog.value = true
                        } else {
                            onSwitchAlarm(isChecked)
                        }
                    },
                )
            }

            TextSwitchRow(
                modifier = modifier,
                text = "雨雪時にアラームを早める",
                isChecked = alarmItemState.isWeatherForecastOn,
                onSwitch = {
                    onSwitchWeatherForecast(it)
                    if (alarmItemState.isWeatherForecastOn) {
                        fetchWeather()
                    }
                },
            )

            RequestExactAlarmPermission(openDialog = openAlarmPermissionDialog)

            if (alarmUiState.expandedAlarmItem) {
                Column(modifier = modifier) {
                    RadioButtonGroup(
                        modifier = modifier,
                        radioOptions = radioOptions,
                        selectedOption = alarmItemState.selectedEarlyAlarmTime.substringAfter(":"),
                        onOptionSelected = { String ->
                            if (!alarmItemState.isWeatherForecastOn) {
                                selectRadioButton("00:00")
                            } else {
                                selectRadioButton("00:$String")
                            }
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
fun AlarmTimeRow(
    modifier: Modifier,
    toggleShowTimePicker: () -> Unit,
    alarmText: String,
    changedAlarmText: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier =
                modifier.clickable {
                    toggleShowTimePicker()
                },
            text = alarmText,
            fontSize = 40.sp,
        )
        Text(
            text = " -> $changedAlarmText",
            fontSize = 40.sp,
        )

        Spacer(modifier = modifier.weight(1f))
        ExpandButton(
            modifier = modifier,
            expanded = expanded,
            onClick = {
                onExpandToggle()
            },
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
        onOptionSelected = { String ->
            selectedOption.value = String
        },
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

@Preview
@Composable
fun AlarmItemPreview() {
    val context = LocalContext.current
    val alarmManager = remember { context.applicationContext.getSystemService(AlarmManager::class.java) as AlarmManager }

    AlarmItem(
        modifier = Modifier,
        alarmUiState =
            AlarmUiState(
                alarmItemState =
                    AlarmItemState(
                        id = 0,
                        alarmTime = "07:00",
                        selectedEarlyAlarmTime = "00:00",
                        changedAlarmTImeByWeather = "07:00",
                        isAlarmOn = true,
                        isWeatherForecastOn = false,
                    ),
                expandedAlarmItem = false,
            ),
        weatherState = WeatherState.Initial,
        expandedAlarmItem = { },
        updateUntilTime = { hours, minutes -> },
        onSwitchAlarm = { Boolean -> },
        onSwitchWeatherForecast = { Boolean -> },
        isBadWeather = { Boolean -> },
        selectTime = { String -> },
        selectRadioButton = { String -> },
        onDeleteAlarm = { },
        fetchWeather = { },
        alarmManager = alarmManager,
    )
}
