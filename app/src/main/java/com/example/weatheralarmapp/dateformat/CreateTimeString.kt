package com.example.weatheralarmapp.dateformat

fun createMinuteString(minute: Int): String =
    if (minute < 10) {
        if (minute == 0) "00" else "0$minute"
    } else {
        minute.toString()
    }

fun createHourString(hour: Int): String =
    if (hour < 10) {
        "0$hour"
    } else {
        hour.toString()
    }
