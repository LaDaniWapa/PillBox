package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.data.models.DBMedication
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import cafe.adriel.voyager.navigator.Navigator

class MedicationDetailsScreen(
    private val medication: DBMedication
) : BaseScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        MedicationDetailsContent(medication, navigator)
    }

    @Composable
    private fun MedicationDetailsContent(
        medication: DBMedication,
        navigator: Navigator? = null
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator?.pop() }) {
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

            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with name and type
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

                // Details
                Card(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
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

                        // Document ID (for debugging/development)
                        if (medication.docId != null) {
                            DetailRow(
                                label = "Document ID",
                                value = medication.docId,
                                textStyle = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Edit */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }

                    Button(
                        onClick = { /* Refill */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Schedule")
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
        Column(modifier = modifier) {
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
}