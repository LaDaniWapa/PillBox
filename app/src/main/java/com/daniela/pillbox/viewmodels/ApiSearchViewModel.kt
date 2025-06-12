package com.daniela.pillbox.viewmodels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.models.ApiMedication
import com.daniela.pillbox.data.repository.ApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ApiSearchViewModel(
    private val apiRepository: ApiRepository,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // UiState
    private val _uiState = mutableStateOf(ApiSearchUiState())
    val uiState: State<ApiSearchUiState> = _uiState

    private fun updateUiState(update: ApiSearchUiState.() -> ApiSearchUiState) {
        _uiState.value = _uiState.value.update()
    }

    /**
     * Called when the user changes the search query.
     * @param query The new search query.
     */
    fun onSearchQueryChanged(query: String) = updateUiState { copy(searchQuery = query) }

    /**
     * Called when the user submits a search query.
     * @param query The search query.
     */
    fun searchMedications(query: String) {
        updateUiState { copy(isLoading = true, error = null, searchResults = emptyList()) }

        coroutineScope.launch {
            try {
                val results = apiRepository.searchMedications(query)
                updateUiState { copy(searchResults = results, isLoading = false) }
            } catch (e: Exception) {
                updateUiState { copy(error = e.message, isLoading = false) }
            }
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
     * UiState for the ApiSearchScreen.
     * @property searchResults The list of medication objects that match the search query.
     * @property searchQuery The current search query.
     * @property isLoading Whether the screen is currently loading.
     */
    data class ApiSearchUiState(
        val searchResults: List<ApiMedication> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )
}