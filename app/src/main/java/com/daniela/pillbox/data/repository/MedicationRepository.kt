package com.daniela.pillbox.data.repository

import io.appwrite.Query
import android.content.Context
import android.util.Log
import com.daniela.pillbox.Appwrite
import com.daniela.pillbox.BuildConfig
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.Schedule

/**
 * Handles Medication_db crud operations
 */
class MedicationRepository(val ctx: Context) {
    /**
     * Gets all medications for a specific user
     */
    suspend fun getUserMedications(userId: String): List<Medication> {
        val db = Appwrite.getDatabases(ctx)
        val documents = db.listDocuments(
            databaseId = BuildConfig.DATABASE_ID,
            collectionId = BuildConfig.MEDICATIONS_ID,
            queries = listOf(
                Query.equal("userId", userId)
            ),
        )

        // we getting some action, yaass ✨✨
        Log.i("TAG", "getUserMedications: $documents")

        // return dummy data
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
