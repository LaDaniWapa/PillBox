package com.daniela.pillbox.data.models

import java.io.Serializable

// Objects for transferring data

/**
 * Interface for medication objects.
 */
interface BaseMedication {
    val userId: String
    val name: String
    val dosage: String
    val dosageUnit: String
    val type: String
    val stock: Int?
    val instructions: String?
    val notes: String?
    val color: String?
}

/**
 * Medication object.
 */
data class Medication(
    override val userId: String,
    override val name: String,
    override val dosage: String,
    override val dosageUnit: String,
    override val type: String,
    override val stock: Int? = null,
    override val instructions: String? = null,
    override val notes: String? = null,
    override val color: String? = null,
) : BaseMedication, Serializable

/**
 * Adds a document ID to a Medication object.
 */
fun Medication.withDocId(docId: String) = MedicationWithDocId(
    docId = docId,
    userId = this.userId,
    name = this.name,
    dosage = this.dosage,
    dosageUnit = this.dosageUnit,
    type = this.type,
    stock = this.stock,
    instructions = this.instructions,
    notes = this.notes,
    color = this.color
)

/**
 * Medication object with a document ID.
 */
data class MedicationWithDocId(
    val docId: String? = null,
    override val userId: String,
    override val name: String,
    override val dosage: String,
    override val dosageUnit: String,
    override val type: String,
    override val stock: Int? = null,
    override val instructions: String? = null,
    override val notes: String? = null,
    override val color: String? = null,
) : BaseMedication, Serializable

/**
 * Converts a MedicationWithDocId object to a Medication object.
 */
fun MedicationWithDocId.toMedication() = Medication(
    userId = this.userId,
    name = this.name,
    dosage = this.dosage,
    dosageUnit = this.dosageUnit,
    type = this.type,
    stock = this.stock,
    instructions = this.instructions,
    notes = this.notes,
    color = this.color
)

/**
 * Interface for schedule objects.
 */
interface BaseSchedule {
    val weekDays: List<Int>?
    val times: List<String>?
    val amounts: List<Int>?
    val asNeeded: Boolean
    val medicationId: String
}

/**
 * Schedule object.
 */
data class Schedule(
    override val weekDays: List<Int>? = null, // 0 - 6
    override val times: List<String>? = null,
    override val amounts: List<Int>? = null,
    override val asNeeded: Boolean = false,
    override val medicationId: String = "",
) : BaseSchedule, Serializable

/**
 * Schedule object with a document ID.
 */
data class ScheduleWithDocId(
    val docId: String? = null,
    override val weekDays: List<Int>? = null,
    override val times: List<String>? = listOf("00:00"),
    override val amounts: List<Int>? = listOf(1),
    override val asNeeded: Boolean = false,
    override val medicationId: String = "",
) : BaseSchedule, Serializable

/**
 * Adds a document ID to a Schedule object.
 */
fun Schedule.withDocId(docId: String) = ScheduleWithDocId(
    docId = docId,
    weekDays = this.weekDays,
    times = this.times,
    amounts = this.amounts,
    asNeeded = this.asNeeded,
    medicationId = this.medicationId
)

/**
 * Converts a ScheduleWithDocId object to a Schedule object.
 */
fun ScheduleWithDocId.toSchedule() = Schedule(
    weekDays = this.weekDays,
    times = this.times,
    amounts = this.amounts,
    asNeeded = this.asNeeded,
    medicationId = this.medicationId
)