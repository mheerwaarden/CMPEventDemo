package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

// In androidMain (or wherever your PocketBase dependency is)

import com.github.mheerwaarden.eventdemo.data.model.IBaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// The generic wrapper. It works for any type T that is an IBaseModel.
@Serializable(with = PocketBaseWrapperSerializer::class)
class PocketBaseWrapper<T : IBaseModel>(val data: T) : BaseModel(data.id), IBaseModel by data {
    override val id: String
        get() = data.id
}

// The generic serializer. This is the key to removing the boilerplate.
class PocketBaseWrapperSerializer<T : IBaseModel>(private val dataSerializer: KSerializer<T>) : KSerializer<PocketBaseWrapper<T>> {

    override val descriptor = dataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: PocketBaseWrapper<T>) {
        // Just serialize the wrapped data, not the wrapper itself
        encoder.encodeSerializableValue(dataSerializer, value.data)
    }

    override fun deserialize(decoder: Decoder): PocketBaseWrapper<T> {
        // Deserialize the plain data object
        val deserializedData = decoder.decodeSerializableValue(dataSerializer)
        // Wrap it in our PocketBaseWrapper
        return PocketBaseWrapper(deserializedData)
    }
}
