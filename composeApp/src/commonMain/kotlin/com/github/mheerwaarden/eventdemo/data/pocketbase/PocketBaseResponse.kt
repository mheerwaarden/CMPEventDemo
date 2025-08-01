package com.github.mheerwaarden.eventdemo.data.pocketbase

import kotlinx.serialization.Serializable

@Serializable
data class PocketBaseResponse<T>(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<T>
)