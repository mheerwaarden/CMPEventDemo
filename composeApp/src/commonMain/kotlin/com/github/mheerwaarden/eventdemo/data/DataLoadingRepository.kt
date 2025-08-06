package com.github.mheerwaarden.eventdemo.data

import kotlinx.coroutines.flow.Flow

interface DataLoadingRepository {
    /** Return a flow that keeps the loading state of the data */
    val loadingState: Flow<DataLoadingState>

    fun prepareReload()
}