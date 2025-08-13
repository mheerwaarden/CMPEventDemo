package com.github.mheerwaarden.eventdemo.ui.pocketbaseservice.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.pocketbaseservice.PocketBaseService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val pocketBase: PocketBaseService) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Define the CoroutineExceptionHandler
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // This block will be executed if any exception escapes the viewModelScope.launch blocks
        // where this handler is used, OR if a child coroutine fails and cancels the scope.
        println("AuthViewModel: Coroutine Exception Handler Caught: ${throwable.message}")
        _error.value = "Async error: ${throwable.message ?: "Unknown error"}"
        _isLoading.value = false // Ensure loading state is reset
    }

    fun login(email: String, password: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isLoading.value = true
            pocketBase.login(email, password).fold(
                onSuccess = {
                    _isAuthenticated.value = true
                    _error.value = null
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            pocketBase.register(email, password, name).fold(
                onSuccess = {
                    // Auto-login after registration
                    login(email, password)
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }
}
