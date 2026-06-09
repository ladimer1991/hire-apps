package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val authApiService: AuthApiService = AuthApiService()
) : ViewModel() {

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _selectedServices = mutableStateListOf<String>()
    val selectedServices: List<String> = _selectedServices

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    // Images (Base64 strings)
    private val _images = mutableStateListOf<String>()
    val images: List<String> = _images

    // Credit Card Info
    private val _cardNumber = mutableStateOf("")
    val cardNumber: State<String> = _cardNumber

    private val _cardExpiration = mutableStateOf("")
    val cardExpiration: State<String> = _cardExpiration

    private val _cardCvv = mutableStateOf("")
    val cardCvv: State<String> = _cardCvv

    private val _cardholderName = mutableStateOf("")
    val cardholderName: State<String> = _cardholderName

    // Billing Address
    private val _billingStreet = mutableStateOf("")
    val billingStreet: State<String> = _billingStreet

    private val _billingCity = mutableStateOf("")
    val billingCity: State<String> = _billingCity

    private val _billingState = mutableStateOf("")
    val billingState: State<String> = _billingState

    private val _billingZipCode = mutableStateOf("")
    val billingZipCode: State<String> = _billingZipCode

    private val _billingCountry = mutableStateOf("")
    val billingCountry: State<String> = _billingCountry

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    fun updateDescription(value: String) {
        if (value.length <= 300) {
            _description.value = value
            _errorMessage.value = null
        }
    }

    fun toggleService(service: String) {
        val noneOption = "I will not provide any services"
        if (service == noneOption) {
            if (_selectedServices.contains(noneOption)) {
                _selectedServices.remove(noneOption)
            } else {
                _selectedServices.clear()
                _selectedServices.add(noneOption)
            }
        } else {
            if (_selectedServices.contains(noneOption)) {
                _selectedServices.remove(noneOption)
            }
            if (_selectedServices.contains(service)) {
                _selectedServices.remove(service)
            } else {
                _selectedServices.add(service)
            }
        }
        _errorMessage.value = null
    }

    fun updateEmail(value: String) {
        _email.value = value
        _errorMessage.value = null
    }

    fun updateUsername(value: String) {
        _username.value = value
        _errorMessage.value = null
    }

    fun updatePassword(value: String) {
        _password.value = value
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

    fun updateCardNumber(value: String) { 
        _cardNumber.value = value 
        _errorMessage.value = null
    }
    fun updateCardExpiration(value: String) { 
        val digits = value.filter { it.isDigit() }.take(4)
        val formatted = if (digits.length > 2) {
            "${digits.take(2)}/${digits.drop(2)}"
        } else {
            digits
        }
        _cardExpiration.value = formatted
        _errorMessage.value = null
    }
    fun updateCardCvv(value: String) { 
        _cardCvv.value = value 
        _errorMessage.value = null
    }
    fun updateCardholderName(value: String) { 
        _cardholderName.value = value 
        _errorMessage.value = null
    }

    fun updateBillingStreet(value: String) { 
        _billingStreet.value = value 
        _errorMessage.value = null
    }
    fun updateBillingCity(value: String) { 
        _billingCity.value = value 
        _errorMessage.value = null
    }
    fun updateBillingState(value: String) { 
        val cleanValue = value.filter { it.isLetter() }.take(2).uppercase()
        _billingState.value = cleanValue
        _errorMessage.value = null
    }
    fun updateBillingZipCode(value: String) { 
        _billingZipCode.value = value 
        _errorMessage.value = null
    }
    fun updateBillingCountry(value: String) { 
        _billingCountry.value = value 
        _errorMessage.value = null
    }

    fun register() {
        if (description.value.isBlank()) {
            _errorMessage.value = "Description cannot be empty"
            return
        }
        if (email.value.isBlank()) {
            _errorMessage.value = "Email cannot be empty"
            return
        }
        if (!email.value.contains("@")) {
            _errorMessage.value = "Invalid email format"
            return
        }
        if (username.value.isBlank()) {
            _errorMessage.value = "Username cannot be empty"
            return
        }
        if (password.value.isBlank()) {
            _errorMessage.value = "Password cannot be empty"
            return
        }
        if (password.value.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        // Credit Card Validation
        val hasCardInfo = cardNumber.value.isNotBlank() || cardExpiration.value.isNotBlank() || 
                         cardCvv.value.isNotBlank() || cardholderName.value.isNotBlank()
        
        if (hasCardInfo) {
            if (cardNumber.value.length < 13) {
                _errorMessage.value = "Invalid card number"
                return
            }
            if (!cardExpiration.value.matches(Regex("\\d{2}/\\d{2}"))) {
                _errorMessage.value = "Invalid expiration date (MM/YY)"
                return
            }
            if (cardCvv.value.length < 3) {
                _errorMessage.value = "Invalid CVV"
                return
            }
            if (cardholderName.value.isBlank()) {
                _errorMessage.value = "Cardholder name is required"
                return
            }
        }

        // Billing Address Validation
        val hasBillingInfo = billingStreet.value.isNotBlank() || billingCity.value.isNotBlank() ||
                            billingState.value.isNotBlank() || billingZipCode.value.isNotBlank() ||
                            billingCountry.value.isNotBlank()

        if (hasBillingInfo) {
            if (billingStreet.value.isBlank() || billingCity.value.isBlank() || 
                billingState.value.isBlank() || billingZipCode.value.isBlank() || 
                billingCountry.value.isBlank()) {
                _errorMessage.value = "Complete billing address is required"
                return
            }
        }

        val creditCard = if (hasCardInfo) {
            CreditCardInfo(
                number = cardNumber.value,
                expirationDate = cardExpiration.value,
                cvv = cardCvv.value,
                cardholderName = cardholderName.value
            )
        } else null

        val billingAddress = if (hasBillingInfo) {
            BillingAddress(
                street = billingStreet.value,
                city = billingCity.value,
                state = billingState.value,
                zipCode = billingZipCode.value,
                country = billingCountry.value
            )
        } else null

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            authApiService.register(
                request = RegisterRequest(
                    description = description.value,
                    services = _selectedServices.toList(),
                    email = email.value,
                    username = username.value,
                    password = password.value,
                    images = _images.toList(),
                    creditCard = creditCard,
                    billingAddress = billingAddress
                )
            ).onSuccess { response ->
                _successMessage.value = "Successfully registered ${response.user.username}!"
                clearForm()
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Registration failed"
            }

            _isLoading.value = false
        }
    }

    private fun clearForm() {
        _description.value = ""
        _selectedServices.clear()
        _email.value = ""
        _username.value = ""
        _password.value = ""
        _images.clear()
        _cardNumber.value = ""
        _cardExpiration.value = ""
        _cardCvv.value = ""
        _cardholderName.value = ""
        _billingStreet.value = ""
        _billingCity.value = ""
        _billingState.value = ""
        _billingZipCode.value = ""
        _billingCountry.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        authApiService.close()
    }
}
