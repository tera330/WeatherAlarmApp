package com.example.weatheralarmapp.ui.alarm

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatheralarmapp.ui.common.ExpandButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmItem(modifier: Modifier) {
    val alarmUiState = remember { mutableStateOf(AlarmUiState("8:00", "2時間後にアラームが鳴ります", true)) }
    var expanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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
                    text = alarmUiState.value.alarmTime,
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
                // alarmUiState.value.timeUntilAlarmを参照
                // todo alarmUiState.value.isAlarmOnによって表示を変える
                Text(text = alarmUiState.value.timeUntilAlarm)
                Spacer(modifier = modifier.weight(1f))
                Switch(
                    checked = alarmUiState.value.isAlarmOn,
                    onCheckedChange = { isChecked ->
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
            onConfirm = {
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Preview
@Composable
fun AlarmItemPreview() {
    AlarmItem(
        modifier = Modifier,
    )
}
