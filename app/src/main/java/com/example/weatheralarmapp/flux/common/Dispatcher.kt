package com.example.weatheralarmapp.flux.common

import javax.inject.Inject
import javax.inject.Singleton

typealias DispatchToken = String

private const val _prefix = "ID_"

@Singleton
class Dispatcher
    @Inject
    constructor() {
        private val _callbacks = mutableMapOf<DispatchToken, (Action) -> Unit>()
        private var _lastID = 1

        fun dispatch(payload: Action) {
            for (id in _callbacks.keys) {
                _callbacks[id]?.invoke(payload)
            }
        }

        fun register(callback: (@UnsafeVariance Action) -> Unit): DispatchToken {
            val id = _prefix + _lastID++
            _callbacks[id] = callback
            return id
        }

        fun unregister(id: DispatchToken) {
            require(_callbacks.containsKey(id))
            _callbacks.remove(id)
        }
    }
