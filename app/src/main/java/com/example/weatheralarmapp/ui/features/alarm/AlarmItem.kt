package com.example.weatheralarmapp.ui.features.alarm

import android.app.AlarmManager
import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.weatheralarmapp.ui.common.IndeterminateLinearIndicator
import com.example.weatheralarmapp.ui.features.alarm.components.RequestExactAlarmPermission
import com.example.weatheralarmapp.ui.features.alarm.components.ToggleTimePicker
import com.example.weatheralarmapp.util.dateformat.createHourString
import com.example.weatheralarmapp.util.dateformat.createMinuteString
import java.time.Duration
import java.time.LocalTime
import kotlin.concurrent.timer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(
    modifier: Modifier,
    alarmUiState: AlarmUiState,
    expandedAlarmItem: () -> Unit,
    updateUntilTime: (Int, Long, Long) -> Unit,
    updateUntilAlarmTimeByWeather: (Int, Long, Long) -> Unit,
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

    // ReComposeで全てのアイテムの天気に影響が出てしまっている
    // 応急処置
    if (alarmUiState.weatherState == WeatherState.Initial) {
        fetchWeather()
    }

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
            if (alarmItemState.isAlarmOn) {
                when (alarmUiState.weatherState) {
                    is WeatherState.Error -> {
                        Text("天気予報の取得に失敗しました")
                    }
                    is WeatherState.Success -> {
                        Text(
                            "${alarmUiState.hoursUntilAlarm}時間${alarmUiState.minutesUntilAlarm}分後は${alarmUiState.weatherState.weather}の予報です。",
                        )
                    }
                    else -> {
                        IndeterminateLinearIndicator(
                            modifier = modifier.fillMaxWidth(),
                            loading = true,
                        )
                    }
                }
            }

            AlarmTimeRow(
                modifier = modifier,
                isWeatherForecastOn = alarmItemState.isWeatherForecastOn,
                toggleShowTimePicker = { showTimePicker = !showTimePicker },
                alarmText = alarmItemState.alarmTime,
                changedAlarmText = alarmItemState.changedAlarmTImeByWeather,
                expanded = alarmUiState.expandedAlarmItem,
                onExpandToggle = { expandedAlarmItem() },
                onDeleteAlarm = { onDeleteAlarm() },
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
                            } else { // 7:00のように先頭に0がない場合、0を追加してないと変換できない
                                LocalTime.parse("0${alarmItemState.alarmTime}")
                            }

                        // アラームの設定時間と現在時刻の差分を計算
                        LaunchedEffect(
                            currentTime,
                            alarmItemState.alarmTime,
                            alarmItemState.selectedEarlyAlarmTime,
                            alarmUiState.weatherState,
                        ) {
                            duration =
                                // アラームの設定時間が現在時刻より未来の場合
                                if (alarmTime.isAfter(currentTime) || alarmTime == currentTime) {
                                    Duration.between(currentTime, alarmTime)
                                } else {
                                    // アラームの設定時間が現在時刻より過去の場合
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

                            updateUntilTime(
                                alarmItemState.id,
                                duration.toHours(),
                                duration.toMinutes() % 60,
                            )

                            if (alarmItemState.isWeatherForecastOn) {
                                if (alarmUiState.weatherState is WeatherState.Success) {
                                    if (alarmUiState.weatherState.weather == "小雨" ||
                                        alarmUiState.weatherState.weather == "曇りがち" ||
                                        alarmUiState.weatherState.weather == "曇" ||
                                        alarmUiState.weatherState.weather == "厚い雲" ||
                                        alarmUiState.weatherState.weather == "適度な雨" ||
                                        alarmUiState.weatherState.weather == "雪"
                                        // 晴天
                                    ) {
                                        val selectedEarlyAlarmTime: Long = alarmItemState.selectedEarlyAlarmTime.split(":")[1].toLong()
                                        if (duration.toMinutes() >= selectedEarlyAlarmTime) {
                                            duration =
                                                duration.minus(Duration.ofMinutes(selectedEarlyAlarmTime))
                                        }
                                        isBadWeather(true)
                                    } else {
                                        isBadWeather(false)
                                    }
                                }
                            }

                            if (duration.toMinutes() >= 60) {
                                val hour = duration.toMinutes() / 60
                                val minute = duration.toMinutes() % 60

                                updateUntilAlarmTimeByWeather(alarmItemState.id, hour, minute)
                            } else {
                                val hour = 0.toLong()
                                val minute = duration.toMinutes()

                                updateUntilAlarmTimeByWeather(alarmItemState.id, hour, minute)
                            }
                        }

                        stringResource(
                            R.string.timeUntilAlarm,
                            alarmUiState.earlyHoursUntilAlarmByWeather,
                            alarmUiState.earlyMinutesUntilAlarmByWeather,
                        )
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
                },
            )

            RequestExactAlarmPermission(openDialog = openAlarmPermissionDialog)

            if (alarmItemState.isWeatherForecastOn) {
                Column(modifier = modifier.fillMaxSize()) {
                    RadioButtonGroup(
                        modifier = modifier,
                        radioOptions = radioOptions,
                        selectedOption = alarmItemState.selectedEarlyAlarmTime.substringAfter(":"),
                        onOptionSelected = { String ->
                            selectRadioButton("00:$String")
                        },
                    )
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
    isWeatherForecastOn: Boolean,
    toggleShowTimePicker: () -> Unit,
    alarmText: String,
    changedAlarmText: String,
    onDeleteAlarm: () -> Unit,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier =
                modifier.clickable {
                    toggleShowTimePicker()
                },
            text = alarmText,
            fontSize = 30.sp,
        )
        if (isWeatherForecastOn) {
            Text(
                text = " → $changedAlarmText",
                fontSize = 35.sp,
            )
        }

        Spacer(modifier = modifier.weight(1f))

        IconButton(
            modifier = modifier,
            onClick = {
                onDeleteAlarm()
            },
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
            )
        }
//        ExpandButton(
//            modifier = modifier,
//            expanded = expanded,
//            onClick = {
//                onExpandToggle()
//            },
//        )
    }
}

@Preview
@Composable
fun AlarmTimeRowPreview() {
    AlarmTimeRow(
        modifier = Modifier,
        isWeatherForecastOn = true,
        toggleShowTimePicker = { },
        alarmText = "07:00",
        changedAlarmText = "07:00",
        expanded = false,
        onDeleteAlarm = { },
        onExpandToggle = { },
    )
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
                weatherState = WeatherState.Initial,
                coordinateState = CoordinateState.Initial,
            ),
        expandedAlarmItem = { },
        updateUntilTime = { id, hours, minutes -> },
        updateUntilAlarmTimeByWeather = { id, hours, minutes -> },
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
