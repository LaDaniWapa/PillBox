package com.daniela.pillbox.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.activity.MainActivity
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.Schedule
import com.daniela.pillbox.data.models.toMedication
import com.daniela.pillbox.data.models.toSchedule
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.EmptyCoroutineContext.get

/**
 * ViewModel for managing alarms.
 */
class AlarmViewModel(
    private val alarmScheduler: AlarmScheduler,
    private val medicationRepository: MedicationRepository,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Schedules all alarms for a medication.
     */
    fun scheduleAllAlarmsForMedication(medicationId: String) {
        coroutineScope.launch {
            val medication = medicationRepository.getMedication(medicationId)
            val schedules =
                medicationRepository.getMedicationSchedules(medicationId).map { it.toSchedule() }

            medication?.let { med ->
                alarmScheduler.scheduleAll(med.toMedication(), schedules)
            }
        }
    }

    /**
     * Cancels all alarms for a medication.
     */
    fun cancelAllAlarmsForMedication(medicationId: String) {
        coroutineScope.launch {
            val medication = medicationRepository.getMedication(medicationId)
            val schedules =
                medicationRepository.getMedicationSchedules(medicationId).map { it.toSchedule() }

            medication?.let { med ->
                alarmScheduler.cancelAll(med.toMedication(), schedules)
            }
        }
    }

    /**
     * Updates all alarms for a medication.
     */
    fun updateAllAlarmsForMedication(medicationId: String) {
        cancelAllAlarmsForMedication(medicationId)
        scheduleAllAlarmsForMedication(medicationId)
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     */
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}