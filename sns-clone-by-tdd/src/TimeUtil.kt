package com.harada

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

val formatter: DateTimeFormatter = ISO_OFFSET_DATE_TIME

fun getDateTimeNow(): LocalDateTime = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime()

