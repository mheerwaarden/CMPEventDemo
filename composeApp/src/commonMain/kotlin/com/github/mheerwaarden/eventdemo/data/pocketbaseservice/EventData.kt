/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.EventCategory
import com.github.mheerwaarden.eventdemo.data.model.EventType
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import com.github.mheerwaarden.eventdemo.util.nowInstant
import com.github.mheerwaarden.eventdemo.util.ofEpochMilli
import com.github.mheerwaarden.eventdemo.util.toEpochMilli
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

@Serializable
data class EventData(
    // Event fields
    val id: String = "",
    val collectionId: String = "",
    val collectionName: String = "",
    val created: String = "",
    val updated: String = "",
    val title: String = "",
    val description: String = "",
    val startMillis: Long = nowInstant().toEpochMilliseconds(),
    val endMillis: Long = nowInstant().plus(1, DateTimeUnit.HOUR).toEpochMilliseconds(),
    val location: String? = null,
    val contact: String? = null,
    val notes: String? = null,
    val isOnline: Boolean = false,
    val eventType: EventType = EventType.ACTIVITY,
    val eventCategory: EventCategory = EventCategory.PRIVATE,
    val htmlColor: HtmlColors = HtmlColors.OLIVE_DRAB,
    val amount: Double? = null,
    val price: Double? = null,
    val owner: String = "",
    val viewers: List<String> = emptyList(),
    val isPrivate: Boolean = false
) {
    fun toEvent(): Event = Event(
        id = id,
        collectionId = collectionId,
        collectionName = collectionName,
        created = created,
        updated = updated,
        title = title,
        description = description,
        startDateTime = ofEpochMilli(startMillis),
        endDateTime = ofEpochMilli(endMillis),
        location = location,
        contact = contact,
        notes = notes,
        isOnline = isOnline,
        eventType = eventType,
        eventCategory = eventCategory,
        htmlColor = htmlColor,
        amount = amount,
        price = price,
        owner = owner,
        viewers = viewers,
        isPrivate = isPrivate
    )
}

fun Event.toEventData(owner: String = this.owner): EventData = EventData(
    id = id,
    collectionId = collectionId,
    collectionName = collectionName,
    created = created,
    updated = updated,
    title = title,
    description = description,
    startMillis = startDateTime.toEpochMilli(),
    endMillis = endDateTime.toEpochMilli(),
    location = location,
    contact = contact,
    notes = notes,
    isOnline = isOnline,
    eventType = eventType,
    eventCategory = eventCategory,
    htmlColor = htmlColor,
    amount = amount,
    price = price,
    owner = owner,
    viewers = viewers,
    isPrivate = isPrivate
)
