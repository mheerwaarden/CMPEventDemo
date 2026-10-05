package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient

class EventRepository(userId: String, client: PocketbaseClient) :
    GenericPocketBaseRepository<Event>(
        collectionName = "events",
        userId = userId,
        client = client,
        dataSerializer = Event.serializer()
    ) {

    override suspend fun create(item: Event): PocketBaseResult<Event> {
        val eventWithOwner = item.toEvent().copy(owner = userId)
        return super.create(eventWithOwner)
    }
}