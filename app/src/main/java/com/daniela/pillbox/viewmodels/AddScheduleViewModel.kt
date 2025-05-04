package com.daniela.pillbox.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.ui.screens.AddScheduleScreen.ScheduleEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AddScheduleViewModel(
    private val medsRepository: MedicationRepository,
    private val medicationId: String,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = mutableStateOf(AddScheduleUiState())
    val uiState: State<AddScheduleUiState> = _uiState

    // Helper function to update state
    private fun updateUiState(update: AddScheduleUiState.() -> AddScheduleUiState) {
        _uiState.value = _uiState.value.update()
    }

    fun toggleAsNeeded() {
        updateUiState { copy(asNeeded = !_uiState.value.asNeeded) }
    }

    fun updateEntry(index: Int, entry: ScheduleEntry) {
        val updatedEntries = _uiState.value.scheduleEntries.toMutableList().apply {
            set(index, entry)
        }
        updateUiState { copy(scheduleEntries = updatedEntries) }
    }

    fun removeTimeEntry(index: Int) {
        val updatedEntries = _uiState.value.scheduleEntries.toMutableList().apply {
            removeAt(index)
        }
        updateUiState { copy(scheduleEntries = updatedEntries) }
    }

    fun addScheduleEntry() {
        val updatedEntries = _uiState.value.scheduleEntries + ScheduleEntry()
        updateUiState { copy(scheduleEntries = updatedEntries) }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     */
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }

    /**
     * Represents the UI state for the MedicationDetails screen.
     */
    data class AddScheduleUiState(
        val scheduleEntries: List<ScheduleEntry> = listOf(ScheduleEntry()),
        val asNeeded: Boolean = false,
    )
}