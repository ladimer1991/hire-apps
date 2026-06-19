package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApiService: AuthApiService = AuthApiService()
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    var loggedInUserId: String? = null
        private set

    fun updateEmail(value: String) {
        _email.value = value
        _errorMessage.value = null
    }

    fun updatePassword(value: String) {
        _password.value = value
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(onSuccess: (String) -> Unit) {
        if (email.value.isBlank() || password.value.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authApiService.login(LoginRequest(email.value, password.value)).onSuccess { response ->
                _successMessage.value = "Logged in successfully!"
                loggedInUserId = response.user.id
                onSuccess(response.user.id ?: "")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Login failed"
            }
            _isLoading.value = false
        }
    }
}
