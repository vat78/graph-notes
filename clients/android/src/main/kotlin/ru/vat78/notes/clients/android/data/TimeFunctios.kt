package ru.vat78.notes.clients.android.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.TimeZone

fun currentTimestamp(): Long {
    return Instant.now().epochSecond
}

fun currentTime(): ZonedDateTime {
    return ZonedDateTime.now()
}

fun tomorrow(): ZonedDateTime {
    return LocalDate.now().plusDays(1L).atStartOfDay(TimeZone.getDefault().toZoneId())
}

fun maxTime(): ZonedDateTime {
    return ZonedDateTime.of(LocalDateTime.of(2999, 12, 31, 23, 59), ZoneOffset.UTC)
}

fun minTime(): ZonedDateTime {
    return ZonedDateTime.of(LocalDateTime.of(1970, 1, 1, 0, 0), ZoneOffset.UTC)
}

fun generateTime(rule: TimeDefault, previousFunction: () -> ZonedDateTime = { currentTime() }) : ZonedDateTime {
    return when (rule) {
        TimeDefault.START_OF_TIME -> minTime()
        TimeDefault.PREVIOUS_NOTE -> previousFunction.invoke()
        TimeDefault.NOW -> ZonedDateTime.now()
        TimeDefault.NEXT_MONTH -> ZonedDateTime.of(LocalDateTime.now().plusMonths(1).withDayOfMonth(1), ZoneOffset.UTC)
        TimeDefault.NEXT_YEAR -> ZonedDateTime.of(LocalDateTime.now().plusYears(1).withDayOfYear(1), ZoneOffset.UTC)
        TimeDefault.END_OF_TIME -> maxTime()
    }
}