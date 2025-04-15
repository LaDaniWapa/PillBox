package com.daniela.pillbox.data.repository

import io.appwrite.Query
import android.content.Context
import android.util.Log
import com.daniela.pillbox.Appwrite
import com.daniela.pillbox.BuildConfig
import com.daniela.pillbox.data.models.DBMedication
import io.appwrite.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Handles Medication_db crud operations
 */
class MedicationRepository(val ctx: Context) {
    private val _medications = MutableStateFlow<List<DBMedication>>(emptyList())
    val medications: StateFlow<List<DBMedication>> = _medications

    /**
     * Gets all medications for a specific user
     */
    suspend fun getUserMedications(userId: String) {
        val db = Appwrite.getDatabases(ctx)
        val documents = db.listDocuments(
            databaseId = BuildConfig.DATABASE_ID,
            collectionId = BuildConfig.MEDICATIONS_ID,
            queries = listOf(
                Query.equal("userId", userId)
            ),
            nestedType = DBMedication::class.java
        ).documents

        Log.i("TAG", "getUserMedications: $documents")

        _medications.value = documents.map { d -> d.data }
    }

    /**
     * Adds a medication to the database and update the local medication list
     */
    suspend fun addUserMedication(medication: DBMedication) {
        val db = Appwrite.getDatabases(ctx)

        try {
            db.createDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.MEDICATIONS_ID,
                documentId = ID.unique(),
                data = medication,
                /*permissions = listOf(
                    "read('user:${medication.userId}')",
                    "update('user:${medication.userId}')",
                    "delete('user:${medication.userId}')"
                )*/
            )

            _medications.update { currentList -> currentList + medication }
        } catch (e: Exception) {
            Log.e("TAG", "addUserMedication: $e")
        }

    }
}
