package com.daniela.pillbox.data.repository

import io.appwrite.Query
import android.content.Context
import android.util.Log
import com.daniela.pillbox.Appwrite
import com.daniela.pillbox.BuildConfig
import com.daniela.pillbox.data.models.DBMedication
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.Schedule

/**
 * Handles Medication_db crud operations
 */
class MedicationRepository(val ctx: Context) {
    /**
     * Gets all medications for a specific user
     */
    suspend fun getUserMedications(userId: String): List<DBMedication> {
        val db = Appwrite.getDatabases(ctx)
        val documents = db.listDocuments(
            databaseId = BuildConfig.DATABASE_ID,
            collectionId = BuildConfig.MEDICATIONS_ID,
            queries = listOf(
                Query.equal("userId", userId)
            ),
            nestedType = DBMedication::class.java
        ).documents

        val medsList = documents.map {d->d.data}

        return medsList
    }
}
