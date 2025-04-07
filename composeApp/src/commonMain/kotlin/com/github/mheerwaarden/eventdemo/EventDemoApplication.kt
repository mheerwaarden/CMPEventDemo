/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.github.mheerwaarden.eventdemo

import com.github.mheerwaarden.eventdemo.data.AppContainer
import com.github.mheerwaarden.eventdemo.data.AppDataContainer


object EventDemoApplication {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    var container: AppContainer = AppDataContainer()

    const val TIMEOUT_MILLIS = 5_000L
}