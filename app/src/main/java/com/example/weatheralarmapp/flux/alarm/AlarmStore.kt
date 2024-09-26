package com.example.weatheralarmapp.flux.alarm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.weatheralarmapp.flux.common.Action
import com.example.weatheralarmapp.flux.common.Dispatcher
import com.example.weatheralarmapp.flux.common.Store
import com.example.weatheralarmapp.ui.alarm.AlarmUiState
import java.util.Calendar

class AlarmStore(
    dispatcher: Dispatcher,
) : Store(dispatcher) {
    private val currentTime = Calendar.getInstance()
    private val minutesStr =
        if (currentTime.get(Calendar.MINUTE) < 10) {
            "0${currentTime.get(Calendar.MINUTE)}"
        } else {
            "${currentTime.get(Calendar.MINUTE)}"
        }
    private var _uiState: MutableState<AlarmUiState> =
        mutableStateOf(AlarmUiState("${currentTime.get(Calendar.HOUR_OF_DAY)}:$minutesStr", "", false))
    val uiState: State<AlarmUiState>
        get() = _uiState

    override fun onDispatch(payload: Action) {
        when (payload) {
            is SetAlarmAction.SetAlarmTime -> {
                _uiState.value = _uiState.value.copy(alarmTime = payload.payload)
            }
            is SetAlarmAction.SetAlarmOn -> {
                _uiState.value = _uiState.value.copy(isAlarmOn = payload.payload)
            }
        }
    }
}
