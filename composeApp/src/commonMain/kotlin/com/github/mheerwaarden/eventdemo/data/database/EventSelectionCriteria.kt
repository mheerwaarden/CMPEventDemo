package com.github.mheerwaarden.eventdemo.data.database

import com.github.mheerwaarden.eventdemo.ui.screen.event.EventFilter
import kotlinx.datetime.LocalDateTime

data class EventSelectionCriteria(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val filter: EventFilter
)