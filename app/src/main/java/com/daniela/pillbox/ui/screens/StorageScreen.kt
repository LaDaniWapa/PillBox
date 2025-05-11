package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.MedicationWithDocId
import com.daniela.pillbox.ui.components.DeleteConfirmationDialog
import com.daniela.pillbox.ui.components.FullScreenLoader
import com.daniela.pillbox.utils.capitalized
import com.daniela.pillbox.viewmodels.StorageViewModel

/**
 * Screen for managing medication storage.
 */
class StorageScreen : BaseScreen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val ssh = SavedStateHandle()
        val vm = rememberVoyagerScreenModel<StorageViewModel>(ssh)
        val state by vm.uiState

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = state.isLoading,
            onRefresh = vm::refresh
        ) {
            if (state.showDeleteDialog)
                DeleteConfirmationDialog(
                    description = "Are you sure you want to delete this medication?",
                    title = "Delete medication?",
                    onDismiss = vm::dismissDialog,
                    onConfirm = vm::confirmDeleteMedication
                )

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.medication_storage),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = vm::onSearchQueryChanged,
                        onSearch = { },
                        active = false,
                        onActiveChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart),
                        placeholder = { Text(stringResource(R.string.search_medications)) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { vm.onSearchQueryChanged("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear))
                                }
                            }
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        windowInsets = WindowInsets(0.dp) // Remove system insets
                    ) {
                        // Empty content since we're not using suggestions
                    }
                }

                // Filter chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vm.filters) { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { vm.onFilterSelected(filter) },
                            label = { Text(filter) }
                        )
                    }
                }

                when {
                    state.isLoading -> FullScreenLoader()
                    state.error != null -> ErrorView(state.error)
                    else -> MedicationList(state.filteredMedications, navigator, vm)
                }

            }
        }
    }

    @Composable
    private fun ErrorView(error: String?) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                error ?: stringResource(R.string.there_was_an_error),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    @Composable
    private fun MedicationList(
        filteredMedications: List<MedicationWithDocId>,
        navigator: Navigator,
        vm: StorageViewModel, // Add ViewModel parameter
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Display
            if (filteredMedications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_medications_found), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMedications) { medication ->
                        MedicationStorageItem(
                            medication = medication,
                            onClick = {
                                navigator.push(MedicationDetailsScreen(medication))
                            },
                            onDelete = {
                                medication.docId?.let { docId ->
                                    vm.deleteMedication(docId)
                                }
                            },
                        )
                    }
                }
            }

            // Add button
            FloatingActionButton(
                onClick = { navigator.push(AddMedicationScreen()) },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_medication))
            }
        }
    }
}

/**
 * Medication card item in the storage screen.
 */
@Composable
private fun MedicationStorageItem(
    medication: MedicationWithDocId,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Medication icon/color indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = medication.color?.let { Color(it.toColorInt()) }
                                ?: MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Main info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${medication.dosage}${medication.dosageUnit} ${medication.type.capitalized()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete_medication),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Bottom Row: Notes + Stock
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                // Notes (if exists)
                medication.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))

                // Stock Indicator
                StockIndicator(stock = medication.stock)
            }
        }
    }
}


/**
 * Stock indicator for the medication card.
 */
@Composable
private fun StockIndicator(stock: Int?) {
    // If stock is null, don't show the indicator
    if (stock == null) return
    Badge(
        containerColor = when {
            stock <= 0 -> MaterialTheme.colorScheme.errorContainer
            stock < 5 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when {
            stock <= 0 -> MaterialTheme.colorScheme.onErrorContainer
            stock < 5 -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = when {
                stock <= 0 -> stringResource(R.string.out)
                stock < 5 -> stringResource(R.string.low, stock)
                else -> stringResource(R.string.left, stock)
            },
            style = MaterialTheme.typography.labelSmall
        )
    }
}