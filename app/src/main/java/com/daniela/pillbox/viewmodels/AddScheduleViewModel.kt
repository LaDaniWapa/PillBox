package com.daniela.pillbox.viewmodels

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

    private val _uiState = mutableStateOf(AddScheduleUiState())
    val uiState: State<AddScheduleUiState> = _uiState

    private fun updateUiState(update: AddScheduleUiState.() -> AddScheduleUiState) {
        _uiState.value = _uiState.value.update()
    }

    init {
        schedulesToEdit?.let { schedules ->
            updateUiState {
                copy(
                    schedules = if (schedules.isEmpty())
                        listOf(ScheduleWithDocId())
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
                    listOf(ScheduleWithDocId(asNeeded = true, medicationId = medicationId))
                } else {
                    // When disabling "as needed", add a default schedule
                    listOf(ScheduleWithDocId(medicationId = medicationId))
                }
            )
        }
    }

    fun updateSchedule(index: Int, schedule: ScheduleWithDocId) {
        /*coroutineScope.launch {
            medsRepository.updateMedicationSchedule(schedule.toSchedule(), schedule.docId!!)
        }*/

        val updatedSchedules = _uiState.value.schedules.toMutableList().apply {
            set(index, schedule)
        }
        updateUiState { copy(schedules = updatedSchedules) }
    }

    fun removeSchedule(index: Int) {
        val updatedSchedules = _uiState.value.schedules.toMutableList().apply {
            removeAt(index)
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
        val schedulesToSave = if (_uiState.value.asNeeded) {
            listOf(Schedule(asNeeded = true, medicationId = medicationId))
        } else {
            _uiState.value.schedules.map { schedule ->
                Schedule(
                    weekDays = schedule.weekDays,
                    times = schedule.times,
                    amounts = schedule.amounts,
                    asNeeded = false,
                    medicationId = medicationId
                )
            }
        }

        coroutineScope.launch {
            schedulesToSave.forEach { schedule ->
                medsRepository.addMedicationSchedule(schedule)
            }
        }
    }

    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }

    data class AddScheduleUiState(
        val schedules: List<ScheduleWithDocId> = listOf(ScheduleWithDocId()),
        val asNeeded: Boolean = false,
    )
}