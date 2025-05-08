package com.daniela.pillbox.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
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
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = mutableStateOf(AddScheduleUiState(medicationId))
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
                    asNeeded = schedules.any { it.asNeeded }
                )
            }
        }
    }

    fun toggleAsNeeded() {
        updateUiState {
            copy(
                asNeeded = !_uiState.value.asNeeded,
                schedules = if (!_uiState.value.asNeeded) {
                    // When enabling "as needed", clear other schedules
                    listOf(ScheduleWithDocId(
                        docId = _uiState.value.schedules.firstOrNull()?.docId,
                        asNeeded = true,
                        medicationId = medicationId
                    ))
                } else {
                    // When disabling "as needed", add a default schedule
                    listOf(ScheduleWithDocId(
                        docId = _uiState.value.schedules.firstOrNull()?.docId,
                        medicationId = medicationId
                    ))
                }
            )
        }
    }

    fun updateSchedule(index: Int, schedule: ScheduleWithDocId) {
        Log.i("TAG", "updateSchedule: $schedule")

        val updatedSchedules = _uiState.value.schedules.toMutableList().apply {
            set(index, schedule)
        }
        updateUiState { copy(schedules = updatedSchedules) }
    }

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

    fun addSchedule() {
        val updatedSchedules =
            _uiState.value.schedules + ScheduleWithDocId(medicationId = medicationId)
        updateUiState { copy(schedules = updatedSchedules) }
    }

    fun saveSchedule() {
        Log.i("TAG", "saveSchedule: SAVE SCHEDULES")
        coroutineScope.launch {
            // Process all schedules
            _uiState.value.schedules.forEach { scheduleWithDocId ->
                val schedule = scheduleWithDocId.toSchedule().copy(medicationId = medicationId)
                Log.i("TAG", "saveSchedule: $scheduleWithDocId")

                if (scheduleWithDocId.docId != null) {
                    // Update existing schedule
                    medsRepository.updateMedicationSchedule(
                        docId = scheduleWithDocId.docId,
                        schedule = schedule,
                    )
                } else {
                    Log.i("TAG", "CREATE NEW $schedule")
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

    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }

    data class AddScheduleUiState(
        val schedules: List<ScheduleWithDocId> = emptyList(),
        val asNeeded: Boolean = false,
    ) {
        constructor(medicationId: String) : this(
            schedules = listOf(ScheduleWithDocId(medicationId = medicationId)),
            asNeeded = false
        )
    }
}