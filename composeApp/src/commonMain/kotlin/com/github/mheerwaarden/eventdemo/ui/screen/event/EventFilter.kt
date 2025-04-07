/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.ui.screen.event

import com.github.mheerwaarden.eventdemo.resources.*
import org.jetbrains.compose.resources.StringResource


enum class EventFilter(val text: StringResource) {
    GENERAL(text = Res.string.general),
    CORPORATE(text = Res.string.event_category_corporate),
    PRIVATE(text = Res.string.event_category_private),
    CHARITY(text = Res.string.event_category_charity)
}