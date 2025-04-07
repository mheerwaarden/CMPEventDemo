package com.github.mheerwaarden.eventdemo

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

fun initLogger(): Logger {
    // platformLogWriter() is a relatively simple config option, useful for local debugging. For production
    // uses you *may* want to have a more robust configuration from the native platform. In KaMP Kit,
    // that would likely go into platformModule expect/actual.
    // See https://github.com/touchlab/Kermit
    return Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "ComposeApp")
}