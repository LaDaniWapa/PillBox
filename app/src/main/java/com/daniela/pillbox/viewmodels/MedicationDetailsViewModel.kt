package com.daniela.pillbox.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.models.MedicationWithDocId
import com.daniela.pillbox.data.models.Schedule
import com.daniela.pillbox.data.models.ScheduleWithDocId
import com.daniela.pillbox.data.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

class MedicationDetailsViewModel(
    private val med: MedicationWithDocId,
    private val medsRepository: MedicationRepository,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = mutableStateOf(MedicationDetailsUiState())
    val uiState: State<MedicationDetailsUiState> = _uiState

    // Helper function to update state
    private fun updateUiState(update: MedicationDetailsUiState.() -> MedicationDetailsUiState) {
        _uiState.value = _uiState.value.update()
    }

    init {
        loadSchedules()
    }

    /**
     * Called when the user confirms the deletion of a schedule.
     */
    private fun loadSchedules() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        coroutineScope.launch {
            try {
                med.docId?.let {
                    val schedules = medsRepository.getMedicationSchedules(it)
                    updateUiState { copy(schedules = schedules, isLoading = false) }
                }
            } catch (e: Exception) {
                updateUiState {
                    copy(
                        error = "Failed to load schedules: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }

        }
    }

    /**
     * Called when the user confirms the deletion of a schedule.
     */
    fun addSchedule(schedule: Schedule) {
        updateUiState { copy(isLoading = true) }

        coroutineScope.launch {
            try {
                val newSchedule = medsRepository.addMedicationSchedule(schedule)
                updateUiState {
                    copy(
                        schedules = schedules + newSchedule,
                        isLoading = false,
                        showAddDialog = false
                    )
                }
            } catch (e: Exception) {
                updateUiState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add schedule"
                    )
                }
            }
        }
    }

    /**
     * Called when the user confirms the deletion of a schedule.
     */
    fun updateSchedule(schedule: Schedule, docId: String) {
        updateUiState { copy(isLoading = true) }

        coroutineScope.launch {
            try {
                medsRepository.updateMedicationSchedule(schedule, docId)
                val updatedSchedules = _uiState.value.schedules.map {
                    if (it.docId == docId) it.copy(
                        weekDays = schedule.weekDays,
                        times = schedule.times,
                        amounts = schedule.amounts,
                        asNeeded = schedule.asNeeded
                    ) else it
                }
                updateUiState {
                    copy(
                        schedules = updatedSchedules,
                        isLoading = false,
                        editingSchedule = null
                    )
                }
            } catch (e: Exception) {
                updateUiState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update schedule"
                    )
                }
            }
        }
    }

    /**
     * Called when the user confirms the deletion of a schedule.
     */
    fun deleteSchedule(docId: String) {
        updateUiState { copy(showDeleteDialog = true, selectedSchedule = docId) }
    }

    /**
     * Called when the user confirms the deletion of a schedule.
     */
    fun confirmDeleteSchedule() {
        val docIdToDelete = _uiState.value.selectedSchedule ?: run {
            updateUiState {
                copy(
                    isLoading = false,
                    error = "No schedule selected for deletion",
                    showDeleteDialog = false
                )
            }
            return
        }

        updateUiState { copy(isLoading = true) }

        coroutineScope.launch {
            try {
                medsRepository.deleteMedicationSchedule(docIdToDelete)
                updateUiState {
                    copy(
                        schedules = schedules.filter { it.docId != docIdToDelete },
                        isLoading = false,
                        showDeleteDialog = false,
                        selectedSchedule = null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                updateUiState {
                    copy(
                        isLoading = false,
                        error = "Delete failed: ${e.localizedMessage}",
                        showDeleteDialog = true
                    )
                }
            }
        }
    }

    /**
     * Called when the user confirms the deletion of a schedule.
     */
    fun dismissDialog() {
        updateUiState {
            copy(
                showDeleteDialog = false,
                selectedSchedule = null
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
     * Represents the UI state for the MedicationDetails screen.
     */
    data class MedicationDetailsUiState(
        val schedules: List<ScheduleWithDocId> = emptyList(),
        val showAddDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val editingSchedule: ScheduleWithDocId? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedSchedule: String? = null,
    )
}