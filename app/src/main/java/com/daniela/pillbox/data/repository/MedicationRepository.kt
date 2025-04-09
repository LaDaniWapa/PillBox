package com.daniela.pillbox.data.repository

import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.Schedule

class MedicationRepository {
    suspend fun getUserMedications(userId: String): List<Medication> {
        return listOf(
            Medication(
                id = "1",
                userId = userId,
                name = "Ibuprofen",
                dosage = "200",
                dosageUnit = "mg",
                type = "tablet",
                schedule = Schedule(timesPerDay = 2),
                instructions = "Take with food",
                stock = 3,
                color = "#FF5722"
            ),
            Medication(
                id = "2",
                userId = userId,
                name = "Amoxicillin",
                dosage = "500",
                dosageUnit = "mg",
                type = "capsule",
                schedule = Schedule(timesPerDay = 3),
                instructions = "Complete full course",
                stock = 12,
                notes = "Keep refrigerated"
            )
        )
    }
}