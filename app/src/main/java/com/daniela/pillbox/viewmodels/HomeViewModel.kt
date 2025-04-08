package com.daniela.pillbox.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.utils.capitalized
import io.appwrite.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val ctx: Context,
) : ScreenModel {
    sealed class AuthState {
        object Loading : AuthState()
        data class Authenticated(val user: User<Map<String, Any>>) : AuthState()
        object Unauthenticated : AuthState()
    }

    // Coroutine
    // TODO: Create a BaseScreenModel class and replace this
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val loggedOut = MutableStateFlow(false)

    // Auth variables
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _user = MutableStateFlow<User<Map<String, Any>>?>(null)
    val user: StateFlow<User<Map<String, Any>>?> = _user.asStateFlow()

    // Api handling
    var isLoading by mutableStateOf(true)

    // Attributes
    var showMenu by mutableStateOf(false)

    // Medication List
    private val _medications = mutableStateListOf<Medication>()
    val medications: List<Medication> get() = _medications

    private val _checkedStates = mutableStateMapOf<String, Boolean>()

    fun isMedicationTaken(id: String) = _checkedStates[id] == true

    fun toggleMedication(id: String) {
        _checkedStates[id] = !isMedicationTaken(id)
    }

    init {
        checkAuthState()
        loadMedications()
    }

    // Methods
    private fun checkAuthState() {
        coroutineScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = try {
                authRepository.getLoggedInUser()?.let { user ->
                    _user.value = user
                    AuthState.Authenticated(user)
                } ?: AuthState.Unauthenticated
            } catch (e: Exception) {
                AuthState.Unauthenticated
            }
            isLoading = false
        }
    }

    private fun loadMedications() {
        // TODO: get medications from authRepository
        coroutineScope.launch {
            _medications.addAll(generateSampleMedications())
        }
    }

    fun getGreeting(): String {
        return ctx.getString(
            R.string.greeting_format,
            getTimeBasedGreeting(),
            getDisplayName()
        )
    }

    fun logout() {
        coroutineScope.launch {
            authRepository.logout()
            loggedOut.value = true
        }
    }

    private fun generateSampleMedications() = listOf(
        Medication(
            name = "Aspirin",
            dosage = "81 mg",
            time = "08:00 AM",
            instructions = "With breakfast",
            iconName = "heart"
        ),
        Medication(
            name = "Lisinopril",
            dosage = "10 mg",
            time = "12:00 PM",
            instructions = "With water",
            iconName = "pill"
        ),
        Medication(
            name = "Metformin",
            dosage = "500 mg",
            time = "06:00 PM",
            instructions = "After dinner",
            iconName = "capsule"
        ),
        Medication(
            name = "Aspirin",
            dosage = "81 mg",
            time = "08:00 AM",
            instructions = "With breakfast",
            iconName = "heart"
        ),
        Medication(
            name = "Lisinopril",
            dosage = "10 mg",
            time = "12:00 PM",
            instructions = "With water",
            iconName = "pill"
        ),
        Medication(
            name = "Metformin",
            dosage = "500 mg",
            time = "06:00 PM",
            instructions = "After dinner",
            iconName = "capsule"
        ),
        Medication(
            name = "Aspirin",
            dosage = "81 mg",
            time = "08:00 AM",
            instructions = "With breakfast",
            iconName = "heart"
        ),
        Medication(
            name = "Lisinopril",
            dosage = "10 mg",
            time = "12:00 PM",
            instructions = "With water",
            iconName = "pill"
        ),
        Medication(
            name = "Metformin",
            dosage = "500 mg",
            time = "06:00 PM",
            instructions = "After dinner",
            iconName = "capsule"
        ),
        Medication(
            name = "Aspirin",
            dosage = "81 mg",
            time = "08:00 AM",
            instructions = "With breakfast",
            iconName = "heart"
        ),
        Medication(
            name = "Lisinopril",
            dosage = "10 mg",
            time = "12:00 PM",
            instructions = "With water",
            iconName = "pill"
        ),
        Medication(
            name = "Metformin",
            dosage = "500 mg",
            time = "06:00 PM",
            instructions = "After dinner",
            iconName = "capsule"
        ),
        Medication(
            name = "Aspirin",
            dosage = "81 mg",
            time = "08:00 AM",
            instructions = "With breakfast",
            iconName = "heart"
        ),
        Medication(
            name = "Lisinopril",
            dosage = "10 mg",
            time = "12:00 PM",
            instructions = "With water",
            iconName = "pill"
        ),
        Medication(
            name = "Metformin",
            dosage = "500 mg",
            time = "06:00 PM",
            instructions = "After dinner",
            iconName = "capsule"
        ),
        Medication(
            name = "Aspirin",
            dosage = "81 mg",
            time = "08:00 AM",
            instructions = "With breakfast",
            iconName = "heart"
        ),
        Medication(
            name = "Lisinopril",
            dosage = "10 mg",
            time = "12:00 PM",
            instructions = "With water",
            iconName = "pill"
        ),
        Medication(
            name = "Metformin",
            dosage = "500 mg",
            time = "06:00 PM",
            instructions = "After dinner",
            iconName = "capsule"
        ),
    )

    private fun getTimeBasedGreeting(): String {
        val resources = ctx.resources
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..11 -> resources.getString(R.string.morning_greeting)
            in 12..17 -> resources.getString(R.string.afternoon_greeting)
            in 18..21 -> resources.getString(R.string.evening_greeting)
            else -> resources.getString(R.string.night_greeting)
        }
    }

    private fun getDisplayName(): String {
        val currentUser = _user.value ?: return "User" // Early return if null
        Log.i("TAG", "getDisplayName: $currentUser")

        return when {
            currentUser.name.isNotEmpty() -> currentUser.name.capitalized()
            currentUser.email.isNotEmpty() -> currentUser.email.substringBefore("@").capitalized()
            else -> "User" // Fallback
        }
    }

    // Garbage Collector
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}