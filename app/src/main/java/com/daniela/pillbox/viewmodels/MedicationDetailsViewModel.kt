package com.daniela.pillbox.viewmodels

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

class MedicationDetailsViewModel(
    private val med: MedicationWithDocId,
    private val medsRepository: MedicationRepository,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _schedules = mutableStateListOf<ScheduleWithDocId>()
    val schedules: List<ScheduleWithDocId> get() = _schedules

    // State for dialog visibility
    private val _showAddDialog = mutableStateOf(false)
    val showAddDialog: Boolean get() = _showAddDialog.value

    // State for editing schedule
    private val _editingSchedule = mutableStateOf<ScheduleWithDocId?>(null)
    val editingSchedule: ScheduleWithDocId? get() = _editingSchedule.value

    // Functions to update the state
    fun onShowAddDialogChange(show: Boolean) {
        _showAddDialog.value = show
    }

    fun onEditingScheduleChange(schedule: ScheduleWithDocId?) {
        _editingSchedule.value = schedule
    }

    init {
        loadSchedules()
    }

    private fun loadSchedules() {
        coroutineScope.launch {
            med.docId?.let {
                _schedules.clear()
                _schedules.addAll(medsRepository.getMedicationSchedules(it))
            }
        }
    }

    fun addSchedule(schedule: Schedule) {
        coroutineScope.launch {
            val newSchedule = medsRepository.addMedicationSchedule(schedule)
            _schedules.add(newSchedule)
        }
    }

    fun updateSchedule(schedule: Schedule, docId: String) {
        coroutineScope.launch {
            //medsRepository.updateSchedule(docId, schedule)

            val index = _schedules.indexOfFirst { it.docId == docId }
            if (index != -1) {
                _schedules[index] = _schedules[index].copy(
                    weekDays = schedule.weekDays,
                    times = schedule.times,
                    amounts = schedule.amounts,
                    asNeeded = schedule.asNeeded
                )
            }
        }
    }

    fun deleteSchedule(docId: String) {
        coroutineScope.launch {
            //medsRepository.deleteSchedule(docId)
            _schedules.removeAll { it.docId == docId }
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