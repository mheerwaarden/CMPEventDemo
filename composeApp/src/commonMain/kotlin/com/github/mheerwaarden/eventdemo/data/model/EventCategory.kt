package com.github.mheerwaarden.eventdemo.data.model

import com.github.mheerwaarden.eventdemo.resources.*
import org.jetbrains.compose.resources.StringResource

enum class EventCategory(val text: StringResource) {
    CORPORATE(Res.string.event_category_corporate),
    PRIVATE(Res.string.event_category_private),
    CHARITY(Res.string.event_category_charity)
}