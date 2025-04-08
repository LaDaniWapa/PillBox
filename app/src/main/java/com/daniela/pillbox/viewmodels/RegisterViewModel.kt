package com.daniela.pillbox.viewmodels

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.utils.Helper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val authErrorHandler: Helper,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val registerSuccess = MutableStateFlow(false)

    // Input values
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

    // Api handling
    var isLoading by mutableStateOf(false)
        private set
    var apiError by mutableStateOf<String?>(null)
        private set

    // Setters
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

    fun register() {
        if (!validateInputs()) return

        isLoading = true
        apiError = null

        coroutineScope.launch {
            try {
                val success = authRepository.register(email, password, name)
                if (success)
                    loginAfterRegister()
                else
                    apiError = ctx.getString(R.string.error_409)

            } catch (e: Exception) {
                apiError = authErrorHandler.handleRegistrationError(e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun loginAfterRegister() {
        coroutineScope.launch {
            try {
                val loggedIn = authRepository.login(email, password)
                if (loggedIn)
                    registerSuccess.value = true
                else
                    apiError = ctx.getString(R.string.error_409)

            } catch (e: Exception) {
                apiError = authErrorHandler.handleRegistrationError(e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun validateInputs(): Boolean {
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

        return isValid
    }

    // Garbage Collector
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}