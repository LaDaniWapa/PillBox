package com.daniela.pillbox.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.ActiveIngridient
import com.daniela.pillbox.data.models.Note
import com.daniela.pillbox.viewmodels.ApiDetailsViewModel

class ApiDetailsScreen(
    private val nregistro: String,
) : BaseScreen() {
    @Composable
    override fun Content() {
        val vm = rememberVoyagerScreenModel<ApiDetailsViewModel>(nregistro)
        val state by vm.uiState
        val navigator = LocalNavigator.currentOrThrow

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title
                Text(
                    text = stringResource(R.string.api_details),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            state.medication?.let { med ->
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoRow(
                                label = stringResource(R.string.registration_number),
                                value = med.registrationNumber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    state.medication?.let { med ->
                        // Conditions Section
                        med.drivingProblems?.let { drivingProblems ->
                            SectionTitle(stringResource(R.string.affects_driving))
                            Text(
                                text = if (drivingProblems)
                                    stringResource(R.string.yes)
                                else stringResource(R.string.no),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Supply Section
                        med.supplyProblem?.let { supplyProblems ->
                            SectionTitle(stringResource(R.string.supply_problems))
                            Text(
                                text = if (supplyProblems)
                                    stringResource(R.string.yes)
                                else stringResource(R.string.no),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        SectionTitle(stringResource(R.string.active_ingredients))
                        // Active Principles Section
                        if (!med.activePrinciples.isNullOrEmpty()) {
                            LazyColumn(
                                modifier = Modifier.animateContentSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(med.activePrinciples) { principle ->
                                    PrincipleItem(principle)
                                }
                            }
                        }

                        // Notes Section
                        state.notes?.let { notes ->
                            SectionTitle(stringResource(R.string.notes))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notes) { note ->
                                    NoteItem(note, onClick = { vm.openUrl(it) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(title: String) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(150.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun PrincipleItem(principle: ActiveIngridient) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = MaterialTheme.shapes.small,
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = principle.name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                principle.amount?.let {
                    Text(
                        text = stringResource(R.string.principle_amount, it, principle.unit ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    @Composable
    private fun NoteItem(note: Note, onClick: (String) -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.asunto!!,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (note.url != null && note.url?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onClick(note.url!!) },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.view_document))
                    }
                }
            }
        }
    }
}