package com.daniela.pillbox.data.repository

import android.content.Context
import android.util.Log
import com.daniela.pillbox.Appwrite
import com.daniela.pillbox.BuildConfig
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.MedicationWithDocId
import com.daniela.pillbox.data.models.Schedule
import com.daniela.pillbox.data.models.ScheduleWithDocId
import com.daniela.pillbox.data.models.withDocId
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Handles Medication_db crud operations
 */
class MedicationRepository(val ctx: Context) {
    private val _medications = MutableStateFlow<List<MedicationWithDocId>>(emptyList())
    val medications: StateFlow<List<MedicationWithDocId>> = _medications

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
            nestedType = Medication::class.java
        ).documents

        _medications.value = documents.map { it.data.withDocId(it.id) }
    }

    /**
     * Deletes a medication from the database and update the local medication list
     */
    suspend fun deleteUserMedication(docId: String) {
        val db = Appwrite.getDatabases(ctx)

        try {
            val res = db.deleteDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.MEDICATIONS_ID,
                documentId = docId,
            )

            if (res == true)
                _medications.update { currentList -> currentList.filter { it.docId != docId } }
        } catch (e: Exception) {
            Log.e("TAG", "deleteUserMedication: $e")
        }
    }

    /**
     * Adds a medication to the database and update the local medication list
     */
    suspend fun addUserMedication(medication: Medication) {
        val db = Appwrite.getDatabases(ctx)

        try {
            val res = db.createDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.MEDICATIONS_ID,
                documentId = ID.unique(),
                data = medication,
                nestedType = Medication::class.java
            )

            _medications.update { currentList -> currentList + res.data.withDocId(res.id) }
        } catch (e: Exception) {
            Log.e("TAG", "addUserMedication: $e")
        }
    }

    /**
     * Updates a medication in the database and update the local medication list
     */
    suspend fun updateUserMedication(medication: Medication, docId: String) {
        val db = Appwrite.getDatabases(ctx)

        try {
            val res = db.updateDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.MEDICATIONS_ID,
                documentId = docId,
                data = medication,
                nestedType = Medication::class.java
            )
            val updatedMed = res.data.withDocId(res.id)

            _medications.update { currentList ->
                currentList.map {
                    if (it.docId == docId) updatedMed else it
                }
            }

            Log.i("TAG", "updateUserMedication: update $medication")
        } catch (e: Exception) {
            Log.e("TAG", "updateUserMedication: $e")
        }

    }

    /**
     * Adds a schedule to the database
     */
    suspend fun addMedicationSchedule(schedule: Schedule): ScheduleWithDocId {
        var res: Any? = null

        val db = Appwrite.getDatabases(ctx)
        try {
            res = db.createDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.SCHEDULES_ID,
                documentId = ID.unique(),
                data = schedule,
                nestedType = Schedule::class.java
            )

        } catch (e: Exception) {
            Log.e("TAG", "deleteUserMedication: $e")
        }

        return ScheduleWithDocId()
    }

    /**
     * Deletes a schedule from the database
     */
    suspend fun deleteMedicationSchedule(docId: String): Boolean {
        var res: Any? = null

        val db = Appwrite.getDatabases(ctx)
        try {
            res = db.deleteDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.SCHEDULES_ID,
                documentId = docId,
            )

        } catch (e: Exception) {
            Log.e("TAG", "deleteUserMedication: $e")
        }

        return res == true
    }

    /**
     * Updates a schedule in the database
     */
    suspend fun updateMedicationSchedule(schedule: Schedule, docId: String) {
        var res: Any? = null

        val db = Appwrite.getDatabases(ctx)
        try {
            res = db.updateDocument(
                databaseId = BuildConfig.DATABASE_ID,
                collectionId = BuildConfig.SCHEDULES_ID,
                documentId = docId,
                data = schedule,
                nestedType = Schedule::class.java
            )
            Log.i("TAG", "updateMedicationSchedule: $res")
        } catch (e: Exception) {
            Log.e("TAG", "updateMedicationSchedule: $e")
        }
    }

    /**
     * Gets all schedules for a specific medication
     */
    suspend fun getMedicationSchedules(medicationId: String): List<ScheduleWithDocId> {
        val db = Appwrite.getDatabases(ctx)
        val documents = db.listDocuments(
            databaseId = BuildConfig.DATABASE_ID,
            collectionId = BuildConfig.SCHEDULES_ID,
            queries = listOf(
                Query.equal("medicationId", medicationId)
            ),
            nestedType = Schedule::class.java
        ).documents

        return documents.map { it.data.withDocId(it.id) }
    }
}
