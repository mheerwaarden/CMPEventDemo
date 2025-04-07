/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo.data.model

import org.jetbrains.compose.resources.StringResource

abstract class ModelItem (
    open val id: Long = 0,
) {
    abstract fun getTypeNameResId(): StringResource
    abstract fun getDisplayName(): String
}