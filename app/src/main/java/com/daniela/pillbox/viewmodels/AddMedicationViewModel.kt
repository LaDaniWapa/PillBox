package com.daniela.pillbox.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.Schedule
import com.daniela.pillbox.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the AddMedicationScreen.
 */
class AddMedicationViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Form state
    var name by mutableStateOf(savedStateHandle["name"] ?: "")
        private set

    var dosage by mutableStateOf(savedStateHandle["dosage"] ?: "")
        private set

    var dosageUnit by mutableStateOf(savedStateHandle["dosageUnit"] ?: "mg")
        private set

    var type by mutableStateOf(savedStateHandle["type"] ?: "tablet")
        private set

    var instructions by mutableStateOf(savedStateHandle["instructions"] ?: "")
        private set

    var stock by mutableStateOf(savedStateHandle["stock"] ?: "")
        private set

    var notes by mutableStateOf(savedStateHandle["notes"] ?: "")
        private set

    // Validation state
    val isFormValid: Boolean
        get() = name.isNotBlank() && dosage.isNotBlank() && instructions.isNotBlank()

    // Available options
    val medicationTypes = listOf("tablet", "capsule", "liquid", "injection", "cream", "other")

    // Event handling
    private val _events = MutableSharedFlow<AddMedicationEvent>()
    val events = _events.asSharedFlow()

    // Setters
    fun onNameChange(newValue: String) {
        name = newValue
        savedStateHandle["name"] = newValue
    }

    fun onDosageChange(newValue: String) {
        dosage = newValue
        savedStateHandle["dosage"] = newValue
    }

    fun onDosageUnitChange(newValue: String) {
        dosageUnit = newValue
        savedStateHandle["dosageUnit"] = newValue
    }

    fun onTypeChange(newValue: String) {
        type = newValue
        savedStateHandle["type"] = newValue
    }

    fun onInstructionsChange(newValue: String) {
        instructions = newValue
        savedStateHandle["instructions"] = newValue
    }

    fun onStockChange(newValue: String) {
        stock = newValue
        savedStateHandle["stock"] = newValue
    }

    fun onNotesChange(newValue: String) {
        notes = newValue
        savedStateHandle["notes"] = newValue
    }

    /**
     * Submits the medication form and save it to the database.
     */
    fun onSubmit() {
        if (!isFormValid) return

        // Get userId from current logged in session
        val userID = authRepository.user.value?.id
        if (userID == null) return

        val newMedication = Medication(
            id = "",
            userId = userID,
            name = name,
            dosage = dosage,
            dosageUnit = dosageUnit,
            type = type,
            schedule = Schedule(), // Default schedule
            instructions = instructions,
            stock = stock.toIntOrNull(),
            notes = notes.ifEmpty { null }
        )

        // Send Event
        coroutineScope.launch {
            _events.emit(AddMedicationEvent.SaveMedication(newMedication))
        }
    }

    /**
     * Cancels the form and returns to the previous screen.
     */
    fun onCancel() {
        // Send Event
        coroutineScope.launch {
            _events.emit(AddMedicationEvent.Cancel)
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
     * Events that can be sent to the UI.
     */
    sealed class AddMedicationEvent {
        data class SaveMedication(val medication: Medication) : AddMedicationEvent()
        object Cancel : AddMedicationEvent()
    }
}
