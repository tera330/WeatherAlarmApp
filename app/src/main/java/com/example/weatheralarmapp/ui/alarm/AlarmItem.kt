package com.example.weatheralarmapp.ui.alarm

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.ui.common.ExpandButton
import java.time.Duration
import java.time.LocalTime
import kotlin.concurrent.timer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(
    modifier: Modifier,
    alarmUiState: AlarmUiState,
    onChangeAlarm: (Boolean) -> Unit,
    selectTime: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var hour by remember { mutableLongStateOf(0L) }
    var minute by remember { mutableLongStateOf(0L) }

    Card(modifier = modifier.padding(10.dp)) {
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
                if (alarmUiState.isAlarmOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    var currentTime by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
                    val currentSecond = LocalTime.now().second
                    val secondsUntilNextMinute = 60 - currentSecond

                    timer("updateCurrentTimeTimer", period = 60 * 1000L, initialDelay = secondsUntilNextMinute * 1000L) {
                        currentTime = LocalTime.now().withSecond(0).withNano(0)
                    }

                    val alarmTime =
                        if (alarmUiState.alarmTime.length == 5) {
                            LocalTime.parse(alarmUiState.alarmTime)
                        } else {
                            LocalTime.parse("0${alarmUiState.alarmTime}")
                        }

                    LaunchedEffect(currentTime, alarmUiState.alarmTime) {
                        val duration =
                            if (alarmTime.isAfter(currentTime) || alarmTime == currentTime) {
                                Duration.between(currentTime, alarmTime)
                            } else {
                                val durationUntilMidnight =
                                    Duration.between(currentTime, LocalTime.MIDNIGHT)
                                val durationAfterMidnight =
                                    Duration.between(LocalTime.MIDNIGHT, alarmTime)
                                durationUntilMidnight.plus(durationAfterMidnight + Duration.ofDays(1))
                            }
                        if (duration.toMinutes() >= 60) {
                            hour = duration.toMinutes() / 60
                            minute = duration.toMinutes() % 60
                        } else {
                            hour = 0
                            minute = duration.toMinutes()
                        }
                    }
                    Text(text = stringResource(R.string.timeUntilAlarm, hour, minute))
                } else {
                    Text(text = stringResource(R.string.alarm_of_message))
                }
                Spacer(modifier = modifier.weight(1f))
                Switch(
                    checked = alarmUiState.isAlarmOn,
                    onCheckedChange = {
                        onChangeAlarm(alarmUiState.isAlarmOn)
                    },
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.padding(20.dp))
                // TODO 詳細を追加
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
                        "0${timePicker.minute}"
                    } else {
                        timePicker.minute.toString()
                    }
                if (timePicker.hour < 10) {
                    selectTime("${hourStr.substring(1)}:$minuteStr")
                } else {
                    selectTime("$hourStr:$minuteStr")
                }
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            alarmUiState = alarmUiState,
        )
    }
}

@Preview
@Composable
fun AlarmItemPreview() {
    AlarmItem(
        modifier = Modifier,
        alarmUiState = AlarmUiState("", "", false),
        onChangeAlarm = { Boolean -> },
        selectTime = { String -> },
    )
}
