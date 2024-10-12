package com.example.weatheralarmapp.dateformat

fun createMinuteString(minute: Int): String {
    return if (minute < 10) {
        if (minute == 0) "00" else "0${minute}"
    } else {
        minute.toString()
    }
}

fun createHourString(hour: Int): String {
    return if (hour < 10) {
        "0${hour}"
    } else {
        hour.toString()
    }
}