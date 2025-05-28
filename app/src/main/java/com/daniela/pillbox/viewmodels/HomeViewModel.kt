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
import com.daniela.pillbox.data.models.IntakeWithDocId
import com.daniela.pillbox.data.models.ScheduleWithMedicationAndDocId
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.receivers.AlarmReceiver
import com.daniela.pillbox.utils.AlarmScheduler
import com.daniela.pillbox.utils.capitalized
import io.appwrite.exceptions.AppwriteException
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
    private var intakes: List<IntakeWithDocId> = emptyList()

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
        updateUiState { copy(authState = AuthState.Loading) }

        coroutineScope.launch {
            try {
                val user = authRepository.getLoggedInUser()
                user?.let { user ->
                    updateUiState {
                        copy(
                            authState = AuthState.Authenticated(user),
                            user = user,
                        )
                    }

                    // Load meds after auth, otherwise the medication list is empty
                    loadMedications()
                    getUserIntakes(user.id)
                } ?: run {
                    updateUiState { copy(authState = AuthState.Unauthenticated) }
                }
            } catch (e: AppwriteException) {
                when {
                    // Handle missing account scope specifically
                    e.message?.contains("missing scope (account)") == true -> {
                        updateUiState {
                            copy(authState = AuthState.Error("Please sign in again"))
                        }
                        // Optional: Trigger logout flow
                        authRepository.logout()
                    }
                    // Handle other Appwrite errors
                    else -> {
                        updateUiState {
                            copy(
                                authState = AuthState.Error(
                                    e.message ?: "Authentication error"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateUiState {
                    copy(
                        authState = AuthState.Error(e.message ?: "Unknown error")
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
                when (val currentAuth = _uiState.value.authState) {
                    is AuthState.Authenticated -> {
                        val meds = _uiState.value.user?.id?.let { userId ->
                            medsRepository.getUserMedicationsForToday(userId)
                        }

                        updateUiState {
                            copy(
                                schedulesWithMedications = meds ?: emptyList(),
                                checkedStates = meds?.associate {
                                    Pair(
                                        it.docId!!,
                                        it.times?.get(0)!!
                                    ) to false
                                }
                                    ?: emptyMap(),
                                authState = AuthState.Authenticated(currentAuth.user)
                            )
                        }
                    }

                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e("TAG", "loadMedications: $e")
                updateUiState {
                    copy(
                        authState = AuthState.Error(e.message ?: "Unknown error")
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
        val currentUser = _uiState.value.user ?: return "User"

        return when {
            currentUser.name.isNotEmpty() -> currentUser.name.capitalized()
            currentUser.email.isNotEmpty() -> currentUser.email.substringBefore("@").capitalized()
            else -> "User"
        }
    }

    /**
     * Logs out the current user and sets the loggedOut flag to true.
     */
    fun logout() {
        coroutineScope.launch {
            authRepository.logout()
            updateUiState { copy(authState = AuthState.Unauthenticated) }
        }
    }

    /**
     * Toggles the visibility of the menu.
     */
    fun toggleMenu() {
        updateUiState { copy(showMenu = !showMenu) }
    }

    /**
     * Retrieves the marked medications for the user.
     * @param userId The ID of the user.
     */
    fun getUserIntakes(userId: String) {
        coroutineScope.launch {
            var oldCheckStatus = _uiState.value.checkedStates.toMutableMap()
            medsRepository.getMarkedMedications(userId).forEach { intake ->
                oldCheckStatus[intake.scheduleId to intake.time] = true
            }
            updateUiState { copy(checkedStates = oldCheckStatus) }
        }
    }

    /**
     * Checks if the specific medication instance (schedule + time) has been taken.
     * @param id The ID of the medication.
     * @return True if the medication has been taken, false otherwise.
     */
    fun isMedicationTaken(id: String, time: String): Boolean {
        return _uiState.value.checkedStates.get(id to time) == true
    }

    /**
     * Toggles the checked state of the medication with the given schedule and time.
     * @param med The medication schedule to toggle, must have valid docId and at least one time
     * @throws IllegalArgumentException if med.docId or med.times is null/empty
     */
    fun toggleMedicationChecked(
        med: ScheduleWithMedicationAndDocId,
    ) {
        val id = med.docId ?: run {
            Log.e("MedicationToggle", "Missing document ID for medication")
            return
        }
        val time = med.times?.firstOrNull() ?: run {
            Log.e("MedicationToggle", "No times available for medication $id")
            return
        }

        coroutineScope.launch {
            val newCheckedState = !isMedicationTaken(id, time)

            try {
                // Optimistic UI update
                updateUiState {
                    copy(
                        checkedStates = checkedStates.toMutableMap().apply {
                            put(id to time, newCheckedState)
                        }
                    )
                }

                // Perform repository operation
                if (newCheckedState) {
                    medsRepository.markMedicationAsTaken(med)
                } else {
                    medsRepository.markMedicationAsNOTTaken(med)
                }

                Log.d(
                    "MedicationToggle",
                    "Successfully ${if (newCheckedState) "marked" else "unmarked"} medication $id at $time"
                )

            } catch (e: Exception) {
                Log.e(
                    "MedicationToggle",
                    "Failed to toggle medication $id at $time: ${e.message}", e
                )

                // Revert UI state on failure
                updateUiState {
                    copy(
                        checkedStates = checkedStates.toMutableMap().apply {
                            put(id to time, !newCheckedState)
                        }
                    )
                }
            }
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

    /* Testing func*/
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

    /* Testing func*/
    fun sendAlarm() {
        coroutineScope.launch {
            println("⏳ Starting alarm test...")

            val activity = ctx as? MainActivity ?: run {
                println("❌ Could not access MainActivity")
                return@launch
            }

            activity.checkAndRequestPermissions(
                action = {
                    try {
                        Log.d("AlarmTest", "Executing alarm scheduling action lambda.")
                        // Generate unique request code each time
                        val requestCode = System.currentTimeMillis().toInt()
                        val alarmTime =
                            Calendar.getInstance().apply { add(Calendar.SECOND, 10) }.timeInMillis

                        println("⏰ Scheduling test alarm #$requestCode for ${Date(alarmTime)}")

                        // Defensive check for ctx (though less likely to be the issue if passed via Koin/constructor)
                        if (ctx == null) {
                            Log.e("AlarmTest", "Context (ctx) is null before creating Intent!")
                            return@checkAndRequestPermissions // or throw an exception to see it clearly
                        }

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

                        val alarmManagerService = ctx.getSystemService(AlarmManager::class.java)
                        if (alarmManagerService == null) {
                            Log.e("AlarmTest", "AlarmManager service is null!")
                            return@checkAndRequestPermissions
                        }

                        alarmManagerService.apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (canScheduleExactAlarms()) {
                                    Log.d("AlarmTest", "Setting exact alarm (SDK >= S)")
                                    setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        alarmTime,
                                        pendingIntent
                                    )
                                } else {
                                    Log.w(
                                        "AlarmTest",
                                        "Cannot schedule exact alarms (SDK >= S), though initial check passed or was skipped."
                                    )
                                }
                            } else {
                                Log.d("AlarmTest", "Setting exact alarm (SDK < S)")
                                setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    alarmTime,
                                    pendingIntent
                                )
                            }
                        }

                        Toast.makeText(ctx, "Alarm #$requestCode scheduled", Toast.LENGTH_SHORT)
                            .show()
                        Log.d(
                            "AlarmTest",
                            "Alarm successfully scheduled in action lambda."
                        ) // <--- ADD LOG

                    } catch (e: Exception) { // <--- ADD CATCH
                        Log.e("AlarmTest", "CRASH INSIDE `sendAlarm`'s action lambda!", e)
                        // Optionally, re-show a toast or provide feedback to the user here
                        Toast.makeText(
                            ctx,
                            "Failed to schedule test alarm: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
        val authState: AuthState = AuthState.Loading,
        val user: User<Map<String, Any>>? = null,
        val showMenu: Boolean = false,
        val schedulesWithMedications: List<ScheduleWithMedicationAndDocId> = emptyList(),
        val checkedStates: Map<Pair<String, String>, Boolean> = emptyMap(),
    )
}
