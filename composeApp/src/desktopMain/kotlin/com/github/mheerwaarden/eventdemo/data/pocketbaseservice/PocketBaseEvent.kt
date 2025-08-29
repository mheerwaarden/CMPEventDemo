package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// PocketBase Kotlin client library requires the data objects to extend BaseModel. Therefore,
// PocketBaseEvent is a wrapper that extends BaseModel and delegates all to eventData.
@Serializable(with = PocketBaseEventSerializer::class)
class PocketBaseEvent(val eventData: IEvent) : BaseModel(eventData.id),
    IEvent by eventData {
    override val id: String
        get() = eventData.id

    override fun toEvent(): Event = eventData.toEvent()
}

private object PocketBaseEventSerializer : KSerializer<PocketBaseEvent> {
    // Use the Event serializer for the delegated data
    private val eventSerializer = Event.serializer()

    // The descriptor for PocketBaseEvent from the perspective of serialization
    // will actually be the descriptor of Event, because it's flattened.
    override val descriptor: SerialDescriptor = eventSerializer.descriptor

    override fun serialize(encoder: Encoder, value: PocketBaseEvent) {
        // When serializing PocketBaseEvent, just serialize its eventData part.
        encoder.encodeSerializableValue(eventSerializer, value.eventData.toEvent())
    }

    override fun deserialize(decoder: Decoder): PocketBaseEvent {
        // When deserializing, expect the JSON to be an Event object.
        val deserializedEvent = decoder.decodeSerializableValue(eventSerializer)
        // Then, use the deserialized Event to construct our PocketBaseEvent wrapper.
        return PocketBaseEvent(deserializedEvent)
    }
}
