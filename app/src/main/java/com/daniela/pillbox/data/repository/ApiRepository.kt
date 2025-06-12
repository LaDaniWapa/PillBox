package com.daniela.pillbox.data.repository

import android.util.Log
import com.daniela.pillbox.data.models.ApiMedication
import com.daniela.pillbox.data.models.ApiMedicationDetails
import com.daniela.pillbox.data.models.MedicationSearchResponse
import com.daniela.pillbox.data.models.Note
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class ApiRepository {
    private val url = "https://cima.aemps.es/cima/rest"

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
                isLenient = true
            })
        }
    }

    suspend fun searchMedications(query: String): List<ApiMedication> {
        return try {
            val response = client.get("$url/medicamentos") {
                parameter("nombre", query)
            }
            response.body<MedicationSearchResponse>().result
        } catch (e: Exception) {
            Log.e("ApiRepository", "Error searching medications: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMedicationDetails(nregistro: String): ApiMedicationDetails? {
        return try {
            val response = client.get("$url/medicamento") {
                parameter("nregistro", nregistro)
            }
            response.body<ApiMedicationDetails>()
        } catch (e: Exception) {
            Log.e("ApiRepository", "Error getting medication details: ${e.message}")
            null
        }
    }

    suspend fun getNotes(nregistro: String): List<Note> {
        return try {
            val response = client.get("$url/notas") {
                parameter("nregistro", nregistro)
            }
            response.body< List<Note>>()
        } catch (e: Exception) {
            Log.e("ApiRepository", "Error getting notes: ${e.message}")
            emptyList()
        }
    }
}