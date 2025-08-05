/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data

import com.github.mheerwaarden.eventdemo.data.database.DummyEventRepository
import com.github.mheerwaarden.eventdemo.data.database.EventRepository

/**
 * App container for Dependency injection.
 * TODO: Move to Koin
 */
interface AppContainer {
    val eventRepository: EventRepository
}

/**
 * [AppContainer] implementation that provides instances of the repositories of the model objects
 */
class AppDataContainer : AppContainer {
    init {
        println("AppDataContainer: init")
    }

    override val eventRepository: EventRepository by lazy {
        DummyEventRepository()
    }

}
