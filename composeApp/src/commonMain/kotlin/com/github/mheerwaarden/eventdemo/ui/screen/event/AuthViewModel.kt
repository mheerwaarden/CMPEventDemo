package com.github.mheerwaarden.eventdemo.ui.screen.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.eventdemo.data.pocketbase.PocketBaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val pocketBase: PocketBaseClient) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
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
