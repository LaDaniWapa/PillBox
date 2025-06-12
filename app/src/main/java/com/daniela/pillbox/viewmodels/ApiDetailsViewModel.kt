package com.daniela.pillbox.viewmodels

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.repository.ApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.daniela.pillbox.data.models.ApiMedicationDetails
import com.daniela.pillbox.data.models.Note

class ApiDetailsViewModel(
    private val nregistro: String,
    private val apiRepository: ApiRepository,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = mutableStateOf(ApiDetailsUiState())
    val uiState: State<ApiDetailsUiState> = _uiState

    private fun updateUiState(update: ApiDetailsUiState.() -> ApiDetailsUiState) {
        _uiState.value = _uiState.value.update()
    }

    init {
        loadMedicationDetails()
        loadNotes()
    }

    /**
     * Loads the medication details from the API.
     */
    fun loadMedicationDetails() {
        updateUiState { copy(isLoading = true, error = null) }
        coroutineScope.launch {
            try {
                val medication = apiRepository.getMedicationDetails(nregistro)
                println(medication)
                updateUiState { copy(medication = medication, isLoading = false) }
            } catch (e: Exception) {
                updateUiState { copy(error = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Loads the notes from the API.
     */
    fun loadNotes() {
        updateUiState { copy(isLoading = true, error = null) }
        coroutineScope.launch {
            try {
                val notes = apiRepository.getNotes(nregistro)
                println(notes)
                updateUiState { copy(notes = notes, isLoading = false) }
            } catch (e: Exception) {
                updateUiState { copy(error = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Opens a URL in the default browser.
     * @param url The URL to open.
     */
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            ctx.startActivity(intent)
        } catch (e: Exception) {
            updateUiState { copy(error = "Cannot open URL: ${e.message}") }
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     */
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }

    /**
     * UiState for the ApiDetailsScreen.
     * @property medication The medication object to display.
     * @property isLoading Whether the screen is currently loading.
     * @property error The error message if there is one.
     */
    data class ApiDetailsUiState(
        val medication: ApiMedicationDetails? = null,
        val notes: List<Note>? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
    )
}