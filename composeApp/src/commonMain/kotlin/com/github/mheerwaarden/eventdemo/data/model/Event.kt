/*
 * Copyright (c) 2025. Event
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.model

import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.event
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import com.github.mheerwaarden.eventdemo.util.now
import com.github.mheerwaarden.eventdemo.util.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    override val id: String = "",
    val collectionId: String = "",
    val collectionName: String = "",
    val created: String = "",
    val updated: String = "",
    val title: String = "",
    val description: String = "",
    val startDateTime: LocalDateTime = now(),
    val endDateTime: LocalDateTime = startDateTime.plus(1, DateTimeUnit.HOUR),
    val location: String? = null,
    val contact: String? = null,
    val notes: String? = null,
    val eventType: EventType = EventType.ACTIVITY,
    val eventCategory: EventCategory = EventCategory.PRIVATE,
    val isOnline: Boolean = false,
    val htmlColor: HtmlColors = HtmlColors.OLIVE_DRAB,
    val amount: Double? = null,
    val price: Double? = null,
    val owner: String = "",
    val viewers: List<String> = emptyList(),
    val isPrivate: Boolean = false
) : ModelItem() {
    companion object {
        val typeNameResId = Res.string.event
    }

    override fun getTypeNameResId() = typeNameResId
    override fun getDisplayName() = "${eventType.text} $title"
}