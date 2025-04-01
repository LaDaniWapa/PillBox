package com.daniela.pillbox.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.repository.AuthRepository
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
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val loggedOut = MutableStateFlow(false)

    // Attributes
    // val navigator = LocalNavigator.currentOrThrow
    val medications = generateSampleMedications()
    val checkedStates = mutableStateMapOf<Int, Boolean>()
    var showMenu by mutableStateOf(false)

    private val _user = MutableStateFlow<User<Map<String, Any>>?>(null)
    val user: StateFlow<User<Map<String, Any>>?> = _user.asStateFlow()

    fun getGreeting(): String {
        return ctx.getString(
            R.string.greeting_format,
            getTimeBasedGreeting(),
            getDisplayName()
        )
    }

    // Methods
    fun logout() {
        coroutineScope.launch {
            //authRepository.logout()
            loggedOut.value = true
        }
    }

    private fun loadUser() {
        coroutineScope.launch {
            _user.value = authRepository.getLoggedInUser()
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

        return when {
            currentUser.name.isNotEmpty() -> currentUser.name
            currentUser.email.isNotEmpty() -> currentUser.email.substringBefore("@")
            else -> "User" // Fallback
        }
    }

    // Garbage Collector
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}