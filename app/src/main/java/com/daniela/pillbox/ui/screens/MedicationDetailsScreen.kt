package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.data.models.MedicationWithDocId
import com.daniela.pillbox.data.models.ScheduleWithDocId
import com.daniela.pillbox.ui.components.DayIndicator
import com.daniela.pillbox.ui.components.DeleteConfirmationDialog
import com.daniela.pillbox.ui.components.FullScreenLoader
import com.daniela.pillbox.viewmodels.MedicationDetailsViewModel

class MedicationDetailsScreen(
    private val medication: MedicationWithDocId,
) : BaseScreen() {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberVoyagerScreenModel<MedicationDetailsViewModel>(medication)
        val state by vm.uiState

        // Reload schedule list when the screen is resumed
        LaunchedEffect(navigator) {
            // if the screen is at the top of the pile
            if (navigator.items.last() is MedicationDetailsScreen) {
                vm.loadSchedules()
            }
        }

        if (state.showDeleteDialog)
            DeleteConfirmationDialog(
                description = "Are you sure you want to delete this schedule?",
                title = "Delete Schedule?",
                onDismiss = vm::dismissDialog,
                onConfirm = vm::confirmDeleteSchedule
            )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                TopBar(navigator)
            }

            // Content
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with name and type
                    Header()

                    // Details
                    DetailsCard {
                        navigator.replace(AddMedicationScreen(medication))
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navigator.push(AddScheduleScreen(medication.docId!!, state.schedules)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit Schedules")
                        }

                        Button(
                            onClick = { navigator.push(AddScheduleScreen(medication.docId!!)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add Schedule")
                        }
                    }
                }
            }

            // Schedules
            if (state.isLoading)
                item {
                    FullScreenLoader()
                }
            else
                items(state.schedules) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onDelete = {
                            schedule.docId?.let {
                                vm.deleteSchedule(it)
                            }
                        }
                    )
                }
        }
    }

    @Composable
    private fun TopBar(navigator: Navigator) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.pop() }) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Medication Details",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun Header() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = medication.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    @Composable
    private fun DetailsCard(
        onClick: () -> Unit,
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dosage Information
                    DetailRow(
                        label = "Dosage",
                        value = "${medication.dosage} ${medication.dosageUnit}"
                    )

                    // Stock Information with warning for low stock
                    DetailRow(
                        label = "Current Stock",
                        value = when {
                            medication.stock == null -> "Not tracked"
                            medication.stock <= 0 -> "Out of stock"
                            else -> "${medication.stock} remaining"
                        },
                        isWarning = medication.stock?.let { it < 5 } == true
                    )

                    // Medication Type
                    DetailRow(
                        label = "Medication Type",
                        value = medication.type.replaceFirstChar { it.uppercase() }
                    )

                    // Instructions (if available)
                    medication.instructions?.let { instructions ->
                        DetailRow(
                            label = "Instructions",
                            value = instructions,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Notes (if available)
                    medication.notes?.let { notes ->
                        DetailRow(
                            label = "Additional Notes",
                            value = notes,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DetailRow(
        modifier: Modifier = Modifier,
        label: String,
        value: String,
        isWarning: Boolean = false,
        textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    ) {
        Column(modifier = modifier.padding(horizontal = 16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = textStyle,
                color = when {
                    isWarning -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    @Composable
    fun ScheduleItem(
        schedule: ScheduleWithDocId,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Days & buttons row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (schedule.asNeeded)
                        Text(
                            text = "As needed",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    else
                        schedule.weekDays?.let { days ->
                            DayIndicator(days = days)
                        }

                    Spacer(Modifier.weight(1f))

                    // Delete button
                    IconButton(onClick = onDelete, modifier = Modifier.padding(0.dp)) {
                        Icon(
                            Icons.Rounded.Delete,
                            "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Times and amounts
                if (!schedule.asNeeded)
                    schedule.times?.let { times ->
                        val amounts = schedule.amounts ?: List(times.size) { 1 }

                        Column {
                            Text(
                                text = "Times:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            times.zip(amounts).forEach { (time, amount) ->
                                Row {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = "$amount units",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(end = 16.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}