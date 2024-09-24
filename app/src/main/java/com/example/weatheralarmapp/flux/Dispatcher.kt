package com.example.weatheralarmapp.flux

import javax.inject.Inject
import javax.inject.Singleton

typealias DispatchToken = String

private const val prefix = "ID_"

@Singleton
class Dispatcher
@Inject
constructor() {
    private val callbacks = mutableMapOf<DispatchToken, (Action) -> Unit>()
    private var lastID = 1

    fun dispatch(payload: Action) {
        for (id in callbacks.keys) {
            callbacks[id]?.invoke(payload)
        }
    }

    fun register(callback: (@UnsafeVariance Action) -> Unit): DispatchToken {
        val id = prefix + lastID++
        callbacks[id] = callback
        return id
    }

    fun unregister(id: DispatchToken) {
        require(callbacks.containsKey(id))
        callbacks.remove(id)
    }
}