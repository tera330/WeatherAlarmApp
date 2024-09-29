package com.example.weatheralarmapp

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatheralarmapp.data.AlarmItem
import com.example.weatheralarmapp.data.AlarmItemRepository
import com.example.weatheralarmapp.ui.alarm.AlarmUiState
import com.example.weatheralarmapp.ui.home.HomeUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class AlarmViewModel(
    private val alarmItemRepository: AlarmItemRepository,
) : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val currentTime = Calendar.getInstance()
    private val minutesStr =
        if (currentTime.get(Calendar.MINUTE) < 10) {
            if (currentTime.get(Calendar.MINUTE) == 0) "00" else "0${currentTime.get(Calendar.MINUTE)}"
        } else {
            "${currentTime.get(Calendar.MINUTE)}"
        }
    private var _alarmUiState: MutableState<AlarmUiState> =
        mutableStateOf(AlarmUiState(0, "${currentTime.get(Calendar.HOUR_OF_DAY)}:$minutesStr", false))
    val alarmUiState: State<AlarmUiState>
        get() = _alarmUiState

    val homeUiState: StateFlow<HomeUiState> =
        alarmItemRepository
            .getAllAlarmItemsStream()
            .map { HomeUiState(it) }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState(),
            )

    suspend fun addAlarmItem(alarmItem: AlarmItem) {
        alarmItemRepository.insertAlarmItem(alarmItem)
    }

    suspend fun updateAlarmItem(alarmItem: AlarmItem) {
        alarmItemRepository.updateAlarmItem(alarmItem)
    }

    suspend fun deleteAlarmItem(alarmItem: AlarmItem) {
        alarmItemRepository.deleteAlarmItem(alarmItem)
    }
}
