package com.example.weatheralarmapp.ui.features.alarm

import com.example.weatheralarmapp.data.local.AlarmItem

data class HomeUiState(
    val alarmItemList: List<AlarmItem> = listOf(),
)
