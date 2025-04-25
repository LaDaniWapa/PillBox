package com.daniela.pillbox.data.models

import java.io.Serializable

// Objects for transferring data

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

interface BaseSchedule {
    val weekDays: List<Int>?    // [0, 1, 2, ... 6]
    val times: List<String>?   // ["08:00", "12:00", ...]
    val amounts: List<Int>?     // [1, 2, 3, ...]
    val asNeeded: Boolean       // true if the schedule is for as needed medication
}

data class Schedule(
    override val weekDays: List<Int>? = null,
    override val times: List<String>? = null,
    override val amounts: List<Int>? = null,
    override val asNeeded: Boolean = false,
) : BaseSchedule, Serializable

data class ScheduleWithDocId(
    val docId: String? = null,
    override val weekDays: List<Int>? = null,
    override val times: List<String>? = null,
    override val amounts: List<Int>? = null,
    override val asNeeded: Boolean = false,
) : BaseSchedule, Serializable

fun Schedule.withDocId(docId: String) = ScheduleWithDocId(
    docId = docId,
    weekDays = this.weekDays,
    times = this.times,
    amounts = this.amounts,
    asNeeded = this.asNeeded
)