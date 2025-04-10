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
import com.daniela.pillbox.data.models.Schedule
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

/**
 * ViewModel for the Home screen.
 */
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

    // Setters
    fun isMedicationTaken(id: String) = _checkedStates[id] == true

    fun toggleMedication(id: String) {
        _checkedStates[id] = !isMedicationTaken(id)
    }

    init {
        checkAuthState()
        loadMedications()
    }

    // Methods
    /**
     * Checks the authentication state of the user.
     */
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

    /**
     * Loads the list of medications from the repository.
     */
    private fun loadMedications() {
        // TODO: get medications from authRepository
        coroutineScope.launch {
            _medications.addAll(generateSampleMedications())
        }
    }

    /**
     * Returns a greeting message with the user's name.
     */
    fun getGreeting(): String {
        return ctx.getString(
            R.string.greeting_format,
            getTimeBasedGreeting(),
            getDisplayName()
        )
    }

    /**
     * Logs out the current user and sets the loggedOut flag to true.
     */
    fun logout() {
        coroutineScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Generates a list of sample medications for testing purposes.
     */
    private fun generateSampleMedications() = listOf(
        Medication(
            id = "med123",
            userId = "",
            name = "Amoxicillin",
            dosage = "500",
            dosageUnit = "mg",
            type = "capsule",
            schedule = Schedule(
                timesPerDay = 2,
                specificTimes = listOf("08:00", "20:00"),
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
            ),
            instructions = "Take with food",
            iconName = "pill"
        ),
        Medication(
            id = "med456",
            userId = "",
            name = "Ibuprofen",
            dosage = "200",
            dosageUnit = "mg",
            type = "tablet",
            schedule = Schedule(
                asNeeded = true,
                intervalHours = 6  // Minimum 6 hours between doses
            ),
            instructions = "Take for pain",
            stock = 10  // 10 tablets remaining
        )
    )

    /**
     * Returns the appropriate greeting based on the current time.
     */
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

    /**
     * Returns the display name of the current user.
     */
    private fun getDisplayName(): String {
        val currentUser = _user.value ?: return "User" // Early return if null

        return when {
            currentUser.name.isNotEmpty() -> currentUser.name.capitalized()
            currentUser.email.isNotEmpty() -> currentUser.email.substringBefore("@").capitalized()
            else -> "User" // Fallback
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     */
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}
