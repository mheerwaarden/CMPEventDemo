package com.github.mheerwaarden.eventdemo.data.model

import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import kotlinx.datetime.LocalDateTime

interface IEvent {
    val id: String
    val collectionId: String
    val collectionName: String
    val created: String
    val updated: String
    val title: String
    val description: String
    val startDateTime: LocalDateTime
    val endDateTime: LocalDateTime
    val location: String?
    val contact: String?
    val notes: String?
    val eventType: EventType
    val eventCategory: EventCategory
    val isOnline: Boolean
    val htmlColor: HtmlColors
    val amount: Double?
    val price: Double?
    val owner: String
    val viewers: List<String>
    val isPrivate: Boolean

    fun toEvent(): Event
}