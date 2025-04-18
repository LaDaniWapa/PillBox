package com.daniela.pillbox.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.MedicationWithDocId
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.libs.colorpicker.ext.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the AddMedicationScreen.
 */
class AddMedicationViewModel(
    private val authRepository: AuthRepository,
    private val medsRepository: MedicationRepository,
    private val savedStateHandle: SavedStateHandle,
    private val ctx: Context,
    private val medicationToEdit: MedicationWithDocId?,
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

    var color by mutableStateOf(savedStateHandle["color"] ?: "#ff0000")
        private set

    val success = MutableStateFlow(false)

    // Validation state
    val isFormValid: Boolean
        get() = name.isNotBlank() && dosage.isNotBlank()

    // Available medication types
    val medicationTypes = listOf("tablet", "capsule", "liquid", "injection", "cream", "other")

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

    fun onColorChange(newValue: Color) {
        color = '#' + newValue.toHex(includeAlpha = false)
        savedStateHandle["color"] = color
    }

    init {
        medicationToEdit?.let {
            name = it.name
            dosage = it.dosage.toString()
            dosageUnit = it.dosageUnit
            type = it.type
            instructions = it.instructions ?: ""
            stock = it.stock?.toString() ?: ""
            notes = it.notes ?: ""
            color = it.color ?: "#ff0000"
            savedStateHandle["name"] = name
            savedStateHandle["dosage"] = dosage
            savedStateHandle["dosageUnit"] = dosageUnit
            savedStateHandle["type"] = type
            savedStateHandle["instructions"] = instructions
            savedStateHandle["stock"] = stock
            savedStateHandle["notes"] = notes
        }
    }

    /**
     * Submits the medication form and save it to the database.
     */
    fun onSubmit() {
        if (!isFormValid) return

        // Get userId from current logged in session
        val userID = authRepository.user.value?.id
        if (userID == null) return

        var newMedication = Medication(
            userId = userID,
            name = name,
            dosage = dosage,
            dosageUnit = dosageUnit,
            type = type,
            instructions = instructions.ifEmpty { null },
            stock = stock.toIntOrNull(),
            notes = notes.ifEmpty { null },
            color = color
        )

        medicationToEdit?.let {
            update(newMedication, it.docId)
        } ?: run {
            save(newMedication)
        }

    }

    /**
     * Saves a new medication to the database.
     */
    private fun save(med: Medication) {
        coroutineScope.launch {
            try {
                medsRepository.addUserMedication(med)
                success.value = true
            } catch (e: Exception) {
                Log.e("TAG", "onSubmit: $e")
            }

        }
    }

    /**
     * Updates an existing medication in the database.
     */
    private fun update(med: Medication, docId: String?) {
        if (docId.isNullOrEmpty()) return

        coroutineScope.launch {
            try {
                medsRepository.updateUserMedication(med, docId)
                success.value = true
            } catch (e: Exception) {
                Log.e("TAG", "update: $e")
            }
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
