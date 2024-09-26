package com.example.weatheralarmapp.flux

import androidx.lifecycle.ViewModel
import com.example.weatheralarmapp.flux.alarm.AlarmStore
import com.example.weatheralarmapp.flux.common.Dispatcher

class FluxViewModel(
    dispatcher: Dispatcher,
) : ViewModel() {
    val alarmStore = AlarmStore(dispatcher)
}
