package com.example.weatheralarmapp.ui.home

import com.example.weatheralarmapp.data.AlarmItem

data class HomeUiState(
    val alarmItemList: List<AlarmItem> = listOf(),
)
