package com.daniela.pillbox.viewmodels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.Schedule
import com.daniela.pillbox.data.models.ScheduleWithDocId
import com.daniela.pillbox.data.models.toSchedule
import com.daniela.pillbox.data.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AddScheduleViewModel(
    private val medsRepository: MedicationRepository,
    private val medicationId: String,
    private val schedulesToEdit: List<ScheduleWithDocId>?,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState =
        mutableStateOf(AddScheduleUiState(medicationId, ctx.getString(R.string.new_schedule)))
    val uiState: State<AddScheduleUiState> = _uiState

    private fun updateUiState(update: AddScheduleUiState.() -> AddScheduleUiState) {
        _uiState.value = _uiState.value.update()
    }

    init {
        schedulesToEdit?.let { schedules ->
            updateUiState {
                copy(
                    schedules = if (schedules.isEmpty())
                        listOf(ScheduleWithDocId(medicationId = medicationId))
                    else
                        schedules,
                    asNeeded = schedules.any { it.asNeeded },
                    title = ctx.getString(R.string.edit_schedule)
                )
            }
        }
    }

    /**
     * Toggles the "as needed" flag.
     */
    fun toggleAsNeeded() {
        updateUiState {
            copy(
                asNeeded = !_uiState.value.asNeeded,
                schedules = if (!_uiState.value.asNeeded) {
                    // When enabling "as needed", clear other schedules
                    listOf(
                        ScheduleWithDocId(
                            docId = _uiState.value.schedules.firstOrNull()?.docId,
                            asNeeded = true,
                            medicationId = medicationId
                        )
                    )
                } else {
                    // When disabling "as needed", add a default schedule
                    listOf(
                        ScheduleWithDocId(
                            docId = _uiState.value.schedules.firstOrNull()?.docId,
                            medicationId = medicationId
                        )
                    )
                }
            )
        }
    }

    /**
     * Updates a schedule in the list.
     * todo: algunos no se actualizan correctamente
     */
    fun updateSchedule(index: Int, schedule: ScheduleWithDocId) {
        val updatedSchedules = _uiState.value.schedules.toMutableList().apply {
            set(index, schedule)
        }
        updateUiState { copy(schedules = updatedSchedules) }
    }

    /**
     * Removes a schedule from the list.
     * @param index The index of the schedule to remove.
     */
    fun removeSchedule(index: Int) {
        val scheduleToRemove = _uiState.value.schedules[index]
        val updatedSchedules = _uiState.value.schedules.toMutableList().apply {
            removeAt(index)
        }

        // Delete from DB if it was an existing schedule
        scheduleToRemove.docId?.let { docId ->
            coroutineScope.launch {
                medsRepository.deleteMedicationSchedule(docId)
            }
        }

        updateUiState {
            copy(
                schedules = if (updatedSchedules.isEmpty())
                    listOf(ScheduleWithDocId(medicationId = medicationId))
                else
                    updatedSchedules
            )
        }
    }

    /**
     * Adds a new schedule to the list.
     */
    fun addSchedule() {
        val updatedSchedules =
            _uiState.value.schedules + ScheduleWithDocId(medicationId = medicationId)
        updateUiState { copy(schedules = updatedSchedules) }
    }

    /**
     * Saves the schedule to the database.
     */
    fun saveSchedule() {
        coroutineScope.launch {
            // Process all schedules
            _uiState.value.schedules.forEach { scheduleWithDocId ->
                val schedule = scheduleWithDocId.toSchedule().copy(medicationId = medicationId)

                if (scheduleWithDocId.docId != null) {
                    // Update existing schedule
                    medsRepository.updateMedicationSchedule(
                        docId = scheduleWithDocId.docId,
                        schedule = schedule,
                    )
                } else {
                    // Create new schedule
                    medsRepository.addMedicationSchedule(schedule)
                }
            }

            // Handle "as needed" special case
            if (_uiState.value.asNeeded) {
                val asNeededSchedule = Schedule(
                    asNeeded = true,
                    medicationId = medicationId
                )
                // Find existing "as needed" schedule or create new
                _uiState.value.schedules
                    .firstOrNull { it.asNeeded }
                    ?.docId
                    ?.let { docId ->
                        medsRepository.updateMedicationSchedule(asNeededSchedule, docId)
                    } ?: medsRepository.addMedicationSchedule(asNeededSchedule)
            } else {
                // Delete any existing "as needed" schedule
                _uiState.value.schedules
                    .firstOrNull { it.asNeeded }
                    ?.docId
                    ?.let { docId ->
                        medsRepository.deleteMedicationSchedule(docId)
                    }
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

    /**
     * Data class representing the UI state for adding a schedule.
     */
    data class AddScheduleUiState(
        val schedules: List<ScheduleWithDocId> = emptyList(),
        val asNeeded: Boolean = false,
        val title: String = "",
    ) {
        constructor(medicationId: String, title: String) : this(
            schedules = listOf(ScheduleWithDocId(medicationId = medicationId)),
            asNeeded = false,
            title = title
        )
    }
}