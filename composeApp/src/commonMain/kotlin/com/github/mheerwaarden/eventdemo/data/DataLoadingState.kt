package com.github.mheerwaarden.eventdemo.data

sealed class DataLoadingState<out T> {
    data class Success<T>(val data: T) : DataLoadingState<T>()
    data class Error(val exception: Throwable) : DataLoadingState<Nothing>()
    data object Loading : DataLoadingState<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getDataOrNull(): T? = if (this is Success) data else null
    fun getErrorOrNull(): Throwable? = if (this is Error) exception else null
}