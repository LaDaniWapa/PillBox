package com.daniela.pillbox.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import com.daniela.pillbox.activity.MainActivity
import com.daniela.pillbox.data.models.ScheduleWithMedicationAndDocId
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.receivers.AlarmReceiver
import com.daniela.pillbox.utils.AlarmScheduler
import com.daniela.pillbox.utils.capitalized
import io.appwrite.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val alarmScheduler: AlarmScheduler,
    private val medsRepository: MedicationRepository,
    private val ctx: Context,
) : ScreenModel {
    // Coroutine
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState

    // Helper function to update state
    private fun updateUiState(update: HomeUiState.() -> HomeUiState) {
        _uiState.value = _uiState.value.update()
    }

    init {
        checkAuthState()
    }

    /**
     * Checks the authentication state of the user.
     */
    private fun checkAuthState() {
        updateUiState { copy(authSate = AuthState.Loading) }

        coroutineScope.launch {
            try {
                authRepository.getLoggedInUser()?.let { user ->
                    updateUiState {
                        copy(
                            authSate = AuthState.Authenticated(user),
                            user = user,
                        )
                    }

                    // Load meds after auth, otherwise the medication list is empty
                    loadMedications()
                }
            } catch (e: Exception) {
                updateUiState {
                    copy(
                        authSate = AuthState.Error(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    /**
     * Loads the list of medications from the repository.
     */
    private fun loadMedications() {
        coroutineScope.launch {
            try {
                when (val currentAuth = _uiState.value.authSate) {
                    is AuthState.Authenticated -> {

                        val meds = _uiState.value.user?.id?.let { userId ->
                            medsRepository.getUserMedicationsForToday(userId)
                        }

                        updateUiState {
                            copy(
                                schedulesWithMedications = meds ?: emptyList(),
                                checkedStates = meds?.associate { it.docId!! to false }
                                    ?: emptyMap(),
                                authSate = AuthState.Authenticated(currentAuth.user)
                            )
                        }
                    }

                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e("TAG", "loadMedications: $e")
                updateUiState {
                    copy(
                        authSate = AuthState.Error(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    /**
     * Returns a greeting message with the user's name.
     * @return The greeting message.
     */
    fun getGreeting(): String {
        return ctx.getString(
            R.string.greeting_format,
            getTimeBasedGreeting(),
            getDisplayName()
        )
    }

    /**
     * User's name
     * @return The display name of the current user.
     */
    private fun getDisplayName(): String {
        val currentUser = _uiState.value.user ?: return "User" // Early return if null

        return when {
            currentUser.name.isNotEmpty() -> currentUser.name.capitalized()
            currentUser.email.isNotEmpty() -> currentUser.email.substringBefore("@").capitalized()
            else -> "User" // Fallback
        }
    }


    /**
     * Logs out the current user and sets the loggedOut flag to true.
     */
    fun logout() {
        coroutineScope.launch {
            authRepository.logout()
            updateUiState { copy(authSate = AuthState.Unauthenticated) }
        }
    }

    /**
     * Toggles the visibility of the menu.
     */
    fun toggleMenu() {
        updateUiState { copy(showMenu = !showMenu) }
    }

    /**
     * Checks if the medication with the given ID has been taken.
     * @param id The ID of the medication.
     * @return True if the medication has been taken, false otherwise.
     */
    fun isMedicationTaken(id: String) = _uiState.value.checkedStates[id] == true

    /**
     * Toggles the checked state of the medication with the given ID.
     * @param id The ID of the medication.
     */
    fun toggleMedicationChecked(id: String) {
        updateUiState {
            copy(
                checkedStates = checkedStates.toMutableMap().apply {
                    put(id, checkedStates[id] != true)
                }
            )
        }
    }

    /**
     * Returns the appropriate greeting based on the current time.
     * @return The greeting message.
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

    /**
     * Represents the authentication state of the user.
     */
    sealed class AuthState {
        object Loading : AuthState()
        data class Authenticated(val user: User<Map<String, Any>>) : AuthState()
        object Unauthenticated : AuthState()
        data class Error(val errorMessage: String) : AuthState()
    }

    /**
     * Represents the UI state for the Home screen.
     */
    data class HomeUiState(
        val authSate: AuthState = AuthState.Loading,
        val user: User<Map<String, Any>>? = null,
        val showMenu: Boolean = false,
        val schedulesWithMedications: List<ScheduleWithMedicationAndDocId> = emptyList(),
        val checkedStates: Map<String, Boolean> = emptyMap(),
    )
}
