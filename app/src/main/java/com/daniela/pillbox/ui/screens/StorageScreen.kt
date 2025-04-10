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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.data.models.DBMedication
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

        // Collect the flows as state
        val filteredMedications by vm.filteredMedications.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title
                Text(
                    text = "Medication Storage",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Search bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    SearchBar(
                        query = vm.searchQuery,
                        onQueryChange = vm::onSearchQueryChanged,
                        onSearch = { },
                        active = false,
                        onActiveChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart),
                        placeholder = { Text("Search medications...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (vm.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { vm.onSearchQueryChanged("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
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
                            selected = vm.selectedFilter == filter,
                            onClick = { vm.onFilterSelected(filter) },
                            label = { Text(filter) }
                        )
                    }
                }

                // Medication list
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
                            Text("No medications found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredMedications) { medication ->
                                MedicationStorageItem(medication = medication, onClick = {})
                            }
                        }
                    }

                    // Add button
                    FloatingActionButton(
                        onClick = { navigator.push(AddMedicationScreen()) },
                        modifier = Modifier.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add medication")
                    }
                }
            }
        }
    }
}

/**
 * Medication card item in the storage screen.
 */
@Composable
private fun MedicationStorageItem(
    medication: DBMedication,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
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
                            shape = MaterialTheme.shapes.medium
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
                        text = "${medication.dosage} ${medication.dosageUnit} ${medication.type}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Stock indicator
                StockIndicator(stock = medication.stock)
            }

            // Additional info
            medication.notes?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
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

    // Determine the text and color based on the stock
    val (text, color) = when {
        stock <= 0 -> Pair("Out of stock", MaterialTheme.colorScheme.error)
        stock < 5 -> Pair("$stock left", MaterialTheme.colorScheme.error)
        stock < 10 -> Pair(
            "$stock left",
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )

        else -> Pair("$stock left", MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
    }

    // Display the text with the appropriate color
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = if (stock < 5) FontWeight.Bold else FontWeight.Normal
    )
}
