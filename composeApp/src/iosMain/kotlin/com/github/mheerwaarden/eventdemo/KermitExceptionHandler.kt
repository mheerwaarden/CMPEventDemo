package com.github.mheerwaarden.eventdemo

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler

fun kermitExceptionHandler(log: Logger) = CoroutineExceptionHandler { _, throwable ->
    throwable.printStackTrace()
    log.e(throwable = throwable) { "Error in MainScope" }
}
