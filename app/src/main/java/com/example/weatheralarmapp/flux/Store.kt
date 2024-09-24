package com.example.weatheralarmapp.flux

import androidx.lifecycle.ViewModel

abstract class Store(
    private val dispatcher: Dispatcher,
) : ViewModel() {
    private var _dispatchToken: DispatchToken

    init {
        _dispatchToken =
            dispatcher.register { payload ->
                onDispatch(payload)
            }
    }

    override fun onCleared() {
        dispatcher.unregister(_dispatchToken)
    }

    protected abstract fun onDispatch(payload: Action)
}