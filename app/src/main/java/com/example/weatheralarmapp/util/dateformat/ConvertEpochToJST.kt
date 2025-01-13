package com.example.weatheralarmapp.util.dateformat

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun convertEpochToJST(epochSeconds: Long): String {
    val instant = Instant.ofEpochSecond(epochSeconds)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Tokyo"))
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return dateTime.format(formatter)
}
