package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.ModelItem
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


abstract class GenericPocketBaseRepository<T : ModelItem>(
    protected val collectionName: String,
    protected val userId: String,
    private val client: PocketbaseClient,
    dataSerializer: KSerializer<T>
) {
    // Create a specific serializer instance for our wrapper
    private val wrapperSerializer = PocketBaseWrapperSerializer(dataSerializer)

    open suspend fun create(item: T): PocketBaseResult<T> {
        return try {
            val wrapper = PocketBaseWrapper(item)
            val createdWrapper = client.records.create<PocketBaseWrapper<T>>(
                collectionName,
                Json.encodeToString(wrapperSerializer, wrapper)
            )
            PocketBaseResult.Success(createdWrapper.data)
        } catch (e: Exception) {
            val displayName = item.getDisplayName()
            val errorMessage = "Failed to create $collectionName '$displayName':  " + when (e) {
                is PocketbaseException -> e.reason
                else -> e.message ?: "An unknown error occurred"
            }
            println("GenericPocketBaseRepository: $errorMessage")
            PocketBaseResult.Error(errorMessage)
        }
    }

    // update, delete, get, etc. can be implemented here...

}
