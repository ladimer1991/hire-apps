package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authApiService: AuthApiService = AuthApiService()
) : ViewModel() {

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _providedService = mutableStateOf("")
    val providedService: State<String> = _providedService
    
    private val _hourlyRate = mutableStateOf("")
    val hourlyRate: State<String> = _hourlyRate

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    // Images (Base64 strings)
    private val _images = mutableStateListOf<String>()
    val images: List<String> = _images

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            authApiService.getCurrentUser().onSuccess { user ->
                _username.value = user.username
                _email.value = user.email
                _description.value = user.description ?: ""
                _providedService.value = user.providedService ?: ""
                _hourlyRate.value = user.hourlyRate?.toString() ?: ""
                _images.clear()
                _images.addAll(user.images)
            }.onFailure { error ->
                _errorMessage.value = "Failed to load profile: ${error.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateDescription(value: String) {
        _description.value = value
        _errorMessage.value = null
    }

    fun updateProvidedService(value: String) {
        _providedService.value = value
        _errorMessage.value = null
    }
    
    fun updateHourlyRate(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _hourlyRate.value = value
            _errorMessage.value = null
        }
    }

    fun updateUsername(value: String) {
        _username.value = value
        _errorMessage.value = null
    }

    fun addImage(base64Image: String) {
        if (_images.size < 4) {
            _images.add(base64Image)
        }
    }

    fun removeImage(index: Int) {
        if (index in _images.indices) {
            _images.removeAt(index)
        }
    }

    fun saveProfile() {
        if (username.value.isBlank()) {
            _errorMessage.value = "Username cannot be empty"
            return
        }

        val rate = _hourlyRate.value.toDoubleOrNull()
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            authApiService.updateProfile(
                request = RegisterRequest(
                    username = _username.value,
                    email = _email.value,
                    password = "", // Not updating password here
                    description = _description.value.takeIf { it.isNotBlank() },
                    providedService = _providedService.value.takeIf { it.isNotBlank() },
                    hourlyRate = rate,
                    images = _images.toList()
                )
            ).onSuccess {
                _successMessage.value = "Profile updated successfully!"
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Update failed"
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        authApiService.close()
    }
}
