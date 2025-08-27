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
import com.github.mheerwaarden.eventdemo.util.ofEpochMilli
import com.github.mheerwaarden.eventdemo.util.plus
import com.github.mheerwaarden.eventdemo.util.toEpochMilli
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object LocalDateTimeEpochMillisSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlinx.datetime.LocalDateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeLong(value.toEpochMilli())

    override fun deserialize(decoder: Decoder): LocalDateTime = ofEpochMilli(decoder.decodeLong())
}

@Serializable
data class Event(
    override val id: String = "",
    override val collectionId: String = "",
    override val collectionName: String = "",
    override val created: String = "",
    override val updated: String = "",
    override val title: String = "",
    override val description: String = "",
    @Serializable(with = LocalDateTimeEpochMillisSerializer::class) @SerialName("startMillis") override val startDateTime: LocalDateTime = now(),
    @Serializable(with = LocalDateTimeEpochMillisSerializer::class) @SerialName("endMillis") override val endDateTime: LocalDateTime = startDateTime.plus(1, DateTimeUnit.HOUR),
    override val location: String? = null,
    override val contact: String? = null,
    override val notes: String? = null,
    override val eventType: EventType = EventType.ACTIVITY,
    override val eventCategory: EventCategory = EventCategory.PRIVATE,
    override val isOnline: Boolean = false,
    override val htmlColor: HtmlColors = HtmlColors.OLIVE_DRAB,
    override val amount: Double? = null,
    override val price: Double? = null,
    override val owner: String = "",
    override val viewers: List<String> = emptyList(),
    override val isPrivate: Boolean = false
) : ModelItem(), IEvent {

    override fun toEvent() = this

    companion object {
        val typeNameResId = Res.string.event
    }

    override fun getTypeNameResId() = typeNameResId
    override fun getDisplayName() = "${eventType.text} $title"
}
