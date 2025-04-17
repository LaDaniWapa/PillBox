package com.daniela.pillbox.viewmodels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.models.DBMedication
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.data.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorageViewModel(
    private val authRepository: AuthRepository,
    private val medsRepository: MedicationRepository,
    private val savedStateHandle: SavedStateHandle,
    private val ctx: Context,
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // UiSate
    private val _uiState = mutableStateOf(StorageUiState())
    val uiState: State<StorageUiState> = _uiState

    // Available filters
    val filters = listOf(
        "All",
        "Low Stock",
        "Tablets",
        "Liquids",
        "Capsules",
        "Injections",
        "Creams",
        "Others"
    )

    init {
        loadMedications()
        setupMedicationObserver()
    }

    // Setters
    fun onFilterSelected(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onSortOrderChanged(order: String) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
        applyFilters()
    }

    // Methods
    private fun setupMedicationObserver() {
        coroutineScope.launch {
            medsRepository.medications.collect { meds ->
                _uiState.value =
                    _uiState.value.copy(allMedications = meds, isLoading = false, error = null)
                applyFilters()
            }
        }
    }

    /**
     * Loads the list of medications from the repository.
     */
    fun loadMedications() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        coroutineScope.launch {
            try {
                authRepository.user.value?.id?.let { userId ->
                    medsRepository.getUserMedications(userId)
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        error = "User not authenticated",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load medications: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun deleteMedication(medDocId: String) {
        coroutineScope.launch {
            medsRepository.deleteUserMedication(medDocId)
        }
    }

    /**
     * Applies the current filters to the medication list.
     */
    private fun applyFilters() {
        _uiState.value = _uiState.value.copy(
            filteredMedications = _uiState.value.allMedications
                .filter { med ->
                    // Search filter
                    _uiState.value.searchQuery.isEmpty() ||
                            med.name.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                            med.type.contains(_uiState.value.searchQuery, ignoreCase = true)
                }
                .filter { med ->
                    // Category filter
                    when (_uiState.value.selectedFilter) {
                        "Low Stock" -> med.stock?.let { it < 5 } == true
                        "Tablets" -> med.type.equals("tablet", true)
                        "Liquids" -> med.type.equals("liquid", true)
                        "Capsules" -> med.type.equals("capsule", true)
                        "Injections" -> med.type.equals("injection", true)
                        "Creams" -> med.type.equals("cream", true)
                        "Others" -> med.type.equals("other", true)
                        else -> true // "All"
                    }
                }
                .sortedWith(getSortComparator())

        )
    }

    /**
     * Returns the comparator for sorting the medication list.
     */
    private fun getSortComparator(): Comparator<DBMedication> {
        return when (_uiState.value.sortOrder) {
            "Z-A" -> compareByDescending { it.name }
            "Most Stock" -> compareByDescending { it.stock ?: 0 }
            "Least Stock" -> compareBy { it.stock ?: Int.MAX_VALUE }
            else -> compareBy { it.name } // "A-Z"
        }
    }

    /**
     * Refreshes the medication list.
     * TODO: add pull to refresh or button
     */
    fun refresh() {
        loadMedications()
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     */
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }

    data class StorageUiState(
        val allMedications: List<DBMedication> = emptyList(), // Original unfiltered list
        val filteredMedications: List<DBMedication> = emptyList(), // Filtered results
        val searchQuery: String = "",
        val selectedFilter: String = "All",
        val sortOrder: String = "A-Z",
        val isLoading: Boolean = false,
        val error: String? = null,
    )
}
