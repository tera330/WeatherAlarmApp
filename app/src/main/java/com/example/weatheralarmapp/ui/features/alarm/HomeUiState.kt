package com.example.weatheralarmapp.ui.features.alarm

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class HomeUiState(
    val alarmItemList: SnapshotStateList<AlarmUiState> = mutableStateListOf(),
)
