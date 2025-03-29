package com.daniela.pillbox.viewmodels

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ScreenModel {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // Using the same string resources as registration
    var emailError by mutableStateOf<Int?>(null)
        private set
    var passwordError by mutableStateOf<Int?>(null)
        private set

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun updateEmail(newEmail: String) {
        email = newEmail
        emailError = null
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        passwordError = null
    }

    fun login(): Boolean {
        // Reset errors
        emailError = null
        passwordError = null

        var isValid = true

        if (email.isBlank()) {
            emailError = R.string.error_email_required
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = R.string.error_email_invalid
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = R.string.error_password_required
            isValid = false
        } else if (password.length < 8) {
            passwordError = R.string.error_password_length
            isValid = false
        }

        if (isValid) {
            _loginSuccess.value = true
        }

        return isValid
    }
}