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

data class Schedule(
    val timesPerDay: Int? = null,  // e.g. 2 (twice daily)
    val specificTimes: List<String>? = null,  // e.g. ["08:00", "20:00"]
    val daysOfWeek: List<Int>? = null,  // 1-7 (Monday-Sunday)
    val intervalHours: Int? = null,  // e.g. 12 (every 12 hours)
    val asNeeded: Boolean = false,  // PRN medications
) : Serializable
