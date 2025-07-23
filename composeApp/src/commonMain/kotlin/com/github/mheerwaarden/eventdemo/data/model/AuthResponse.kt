package com.github.mheerwaarden.eventdemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val record: User
)
