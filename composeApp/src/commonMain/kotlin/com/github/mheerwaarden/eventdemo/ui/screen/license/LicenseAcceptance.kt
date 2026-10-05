package com.github.mheerwaarden.eventdemo.ui.screen.license

data class LicenseAcceptance(
    val userId: String,
    // The ISO 8601 string representation
    val timestamp: String,
    val version: String
)