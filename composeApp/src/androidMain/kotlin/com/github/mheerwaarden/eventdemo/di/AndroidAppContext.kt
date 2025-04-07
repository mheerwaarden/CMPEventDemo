package com.github.mheerwaarden.eventdemo.di

import android.content.Context
import android.text.format.DateFormat
import com.github.mheerwaarden.eventdemo.AppContext

class AndroidAppContext(private val context: Context) : AppContext {
    override val is24HourFormat: Boolean
        get() = DateFormat.is24HourFormat(context)

}