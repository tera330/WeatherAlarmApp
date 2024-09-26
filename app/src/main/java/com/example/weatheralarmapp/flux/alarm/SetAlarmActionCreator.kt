package com.example.weatheralarmapp.flux.alarm

import com.example.weatheralarmapp.flux.common.Action
import com.example.weatheralarmapp.flux.common.Dispatcher
import javax.inject.Inject

class SetAlarmActionCreator
    @Inject
    constructor(
        private val dispatcher: Dispatcher,
    ) {
        fun setAlarmTime(time: String) {
            dispatcher.dispatch(SetAlarmAction.SetAlarmTime(time))
        }

        fun setAlarmOn(isOn: Boolean) {
            dispatcher.dispatch(SetAlarmAction.SetAlarmOn(!isOn))
        }
    }

sealed class SetAlarmAction : Action {
    data class SetAlarmTime(
        override val payload: String,
    ) : SetAlarmAction()

    data class SetAlarmOn(
        override val payload: Boolean,
    ) : SetAlarmAction()
}
