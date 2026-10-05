package com.github.mheerwaarden.eventdemo.ui.screen.license

import com.github.mheerwaarden.eventdemo.util.now
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class LicenseViewModel {

    fun acceptLicense(userId: String, version: String): LicenseAcceptance {
        val timestamp = now().toString()
        val record = LicenseAcceptance(userId, timestamp, version)
        // TODO: save record to database or shared storage
        println("License accepted: $record")
        return record
    }
}