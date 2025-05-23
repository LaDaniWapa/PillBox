package com.daniela.pillbox.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import com.daniela.pillbox.activity.MainActivity
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.receivers.AlarmReceiver
import com.daniela.pillbox.utils.AlarmScheduler
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
import java.util.Date

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val alarmScheduler: AlarmScheduler,
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
            } catch (_: Exception) {
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
            userId = "",
            name = "Amoxicillin",
            dosage = "500",
            dosageUnit = "mg",
            type = "capsule",
            instructions = "Take with food",
        ),
        Medication(
            userId = "",
            name = "Ibuprofen",
            dosage = "200",
            dosageUnit = "mg",
            type = "tablet",
            instructions = "Take for pain",
            stock = 10
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

    // Testing func
    fun testAlarmSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ctx.getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("ALARM_TEST", "Exact alarms not permitted")
                Toast.makeText(ctx, "Enable exact alarms in settings", Toast.LENGTH_LONG).show()
                return
            }
        }

        sendAlarm()
        Toast.makeText(ctx, "Test alarm scheduled for 10 seconds from now", Toast.LENGTH_SHORT)
            .show()
    }

    // Testing func
    fun sendAlarm() {
        coroutineScope.launch {
            println("⏳ Starting alarm test...")

            val activity = ctx as? MainActivity ?: run {
                println("❌ Could not access MainActivity")
                return@launch
            }

            activity.checkAndRequestPermissions(
                action = {
                    // Generate unique request code each time
                    val requestCode = System.currentTimeMillis().toInt()
                    val alarmTime =
                        Calendar.getInstance().apply { add(Calendar.SECOND, 10) }.timeInMillis

                    println("⏰ Scheduling test alarm #$requestCode for ${Date(alarmTime)}")

                    val intent = Intent(ctx, AlarmReceiver::class.java).apply {
                        action = "com.daniela.pillbox.TEST_ALARM_$requestCode" // Unique action
                        putExtra("test_alarm", true)
                        putExtra("request_code", requestCode)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        ctx,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    ctx.getSystemService(AlarmManager::class.java).apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (canScheduleExactAlarms()) {
                                setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    alarmTime,
                                    pendingIntent
                                )
                            }
                        } else {
                            setExact(
                                AlarmManager.RTC_WAKEUP,
                                alarmTime,
                                pendingIntent
                            )
                        }
                    }

                    Toast.makeText(ctx, "Alarm #$requestCode scheduled", Toast.LENGTH_SHORT).show()
                },
                onDenied = {
                    println("🔒 Permissions not granted")
                    Toast.makeText(ctx, "Permissions required", Toast.LENGTH_LONG).show()
                }
            )
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
