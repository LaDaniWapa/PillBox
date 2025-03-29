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

class RegisterViewModel : ScreenModel {
    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    // Individual error states
    var nameError by mutableStateOf<Int?>(null)
        private set
    var emailError by mutableStateOf<Int?>(null)
        private set
    var passwordError by mutableStateOf<Int?>(null)
        private set
    var confirmPasswordError by mutableStateOf<Int?>(null)
        private set

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    fun updateName(newName: String) {
        name = newName
        nameError = null
    }

    fun updateEmail(newEmail: String) {
        email = newEmail
        emailError = null
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        passwordError = null
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        confirmPasswordError = null
    }

    fun registerUser(): Boolean {
        // Reset all errors
        nameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null

        var isValid = true

        if (name.isBlank()) {
            nameError = R.string.error_name_required
            isValid = false
        }

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

        if (confirmPassword.isBlank()) {
            confirmPasswordError = R.string.error_confirm_password_required
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = R.string.error_passwords_mismatch
            isValid = false
        }

        if (isValid) {
            _registrationSuccess.value = true
        }

        return isValid
    }
}