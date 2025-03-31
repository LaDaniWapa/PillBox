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

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val authErrorHandler: Helper,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val loginSuccess = MutableStateFlow(false)

    // Input values
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // Individual error states
    var emailError by mutableStateOf<Int?>(null)
        private set
    var passwordError by mutableStateOf<Int?>(null)
        private set

    // Api handling
    var isLoading by mutableStateOf(false)
        private set
    var apiError by mutableStateOf<String?>(null)
        private set

    // Setters
    fun updateEmail(newEmail: String) {
        email = newEmail
        emailError = null
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        passwordError = null
    }

    // Methods
    fun login() {
        if (!validateInputs()) return

        isLoading = true
        apiError = null

        coroutineScope.launch {
            try {
                val success = authRepository.login(email, password)
                if (success)
                    loginSuccess.value = true
                else
                    apiError = ctx.getString(R.string.error_invalid_credentials)
            } catch (e: Exception) {
                apiError = authErrorHandler.handleRegistrationError(e)

            } finally {
                isLoading = false
            }
        }
    }

    suspend fun getLoggedInUser() = authRepository.getLoggedInUser()

    private fun validateInputs(): Boolean {
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

        return isValid
    }

    // Garbage Collector
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}