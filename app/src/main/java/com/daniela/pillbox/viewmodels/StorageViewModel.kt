package com.daniela.pillbox.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.core.model.ScreenModel
import com.daniela.pillbox.data.models.Medication
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
    val authRepository: AuthRepository,
    val medsRepository: MedicationRepository,
    val savedStateHandle: SavedStateHandle,
    val ctx: Context
) : ScreenModel {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // State with SavedStateHandle persistence
    private val _medications = MutableStateFlow(emptyList<Medication>())
    val medications = _medications.asStateFlow()

    private val _filteredMedications = MutableStateFlow(emptyList<Medication>())
    val filteredMedications = _filteredMedications.asStateFlow()

    var selectedFilter by mutableStateOf(
        savedStateHandle.get<String>("selectedFilter") ?: "All"
    )
        private set

    var searchQuery by mutableStateOf(
        savedStateHandle.get<String>("searchQuery") ?: ""
    )
        private set

    var sortOrder by mutableStateOf(
        savedStateHandle.get<String>("sortOrder") ?: "A-Z"
    )
        private set

    var isLoading by mutableStateOf(false)
        private set

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
    }

    fun loadMedications() {
        isLoading = true
        coroutineScope.launch {
            try {
                authRepository.user.value?.id?.let { userId ->
                    val meds = medsRepository.getUserMedications(userId)
                    _medications.value = meds
                    applyFilters()
                }
            } catch (e: Exception) {
                Log.e("TAG", "loadMedications: $e")
            } finally {
                isLoading = false
            }
        }
    }

    fun onFilterSelected(filter: String) {
        selectedFilter = filter
        savedStateHandle["selectedFilter"] = filter
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        savedStateHandle["searchQuery"] = query
        applyFilters()
    }

    fun onSortOrderChanged(order: String) {
        sortOrder = order
        savedStateHandle["sortOrder"] = order
        applyFilters()
    }

    private fun applyFilters() {
        _filteredMedications.value = _medications.value
            .filter { med ->
                // Search filter
                Log.i("TAG", "applyFilters: searchQuery: $searchQuery")
                searchQuery.isEmpty() ||
                        med.name.contains(searchQuery, ignoreCase = true) ||
                        med.type.contains(searchQuery, ignoreCase = true)
            }
            .filter { med ->
                // Category filter
                when (selectedFilter) {
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

        Log.i("TAG", "applyFilters: ${_filteredMedications.value}")
    }

    private fun getSortComparator(): Comparator<Medication> {
        return when (sortOrder) {
            "Z-A" -> compareByDescending { it.name }
            "Most Stock" -> compareByDescending { it.stock ?: 0 }
            "Least Stock" -> compareBy { it.stock ?: Int.MAX_VALUE }
            else -> compareBy { it.name } // "A-Z"
        }
    }

    fun refresh() {
        loadMedications()
    }

    // Garbage Collector
    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}