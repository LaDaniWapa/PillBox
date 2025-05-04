package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.TimePickerButton
import com.daniela.pillbox.viewmodels.AddScheduleViewModel

class AddScheduleScreen(
    private val medicationId: String,
) : BaseScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberVoyagerScreenModel<AddScheduleViewModel>(medicationId)
        val state by vm.uiState

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
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
                    text = "New Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
            }

            // Toggle for "As Needed" schedule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.asNeeded,
                    onCheckedChange = { vm.toggleAsNeeded() }
                )
                Text(
                    text = "Take as needed",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { vm.toggleAsNeeded() }
                )
            }

            // List of schedule entries
            if (!state.asNeeded) {
                state.scheduleEntries.forEachIndexed { index, entry ->
                    ScheduleEntryItem(
                        entry = entry,
                        state = state,
                        index = index,
                        vm = vm
                    )
                }

                // Add new schedule pattern button
                Button(
                    onClick = vm::addScheduleEntry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Another Schedule Pattern")
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {}
                ) {
                    Text("Save Schedule")
                }
            }
        }
    }

    // State for the entire form
    data class ScheduleEntry(
        val days: Set<Int> = emptySet(),
        val timesWithAmounts: List<Pair<String, Int>> = listOf("" to 1),
    )

    @Composable
    private fun ScheduleEntryItem(
        vm: AddScheduleViewModel,
        state: AddScheduleViewModel.AddScheduleUiState,
        entry: ScheduleEntry,
        index: Int,
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            /*colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )*/
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("On these days")
                // Days selection
                WeekDaySelector(
                    selectedDays = entry.days,
                    onDaySelected = { day ->
                        val newDays = if (entry.days.contains(day)) {
                            entry.days - day
                        } else {
                            entry.days + day
                        }
                        vm.updateEntry(index, entry.copy(days = newDays))
                    }
                )

                // Times with amounts
                entry.timesWithAmounts.forEachIndexed { timeIndex, (time, amount) ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Time input
                        TimePickerButton(
                            onTimeSelected = { hour, min ->
                                val newTime = String.format("%02d:%02d", hour, min)
                                val updatedTimes = entry.timesWithAmounts.toMutableList().apply {
                                    set(timeIndex, newTime to amount)
                                }
                                vm.updateEntry(index, entry.copy(timesWithAmounts = updatedTimes))
                            }
                        )

                        // Amount input
                        LabelTextField(
                            modifier = Modifier.width(80.dp),
                            label = "Amount",
                            value = amount.toString(),
                            onValueChange = { newAmount ->
                                val updatedTimes = entry.timesWithAmounts.toMutableList().apply {
                                    set(timeIndex, time to (newAmount.toIntOrNull() ?: 1))
                                }
                                vm.updateEntry(index, entry.copy(timesWithAmounts = updatedTimes))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Remove time button
                        if (timeIndex > 0) {
                            IconButton(onClick = {
                                val updatedTimes = entry.timesWithAmounts.toMutableList().apply {
                                    removeAt(timeIndex)
                                }
                                vm.updateEntry(index, entry.copy(timesWithAmounts = updatedTimes))
                            }) {
                                Icon(Icons.Rounded.Close, "Remove time")
                            }
                        }
                    }
                }

                // Add time button
                Button(
                    onClick = {
                        val updatedTimes = entry.timesWithAmounts + ("" to 1)
                        vm.updateEntry(index, entry.copy(timesWithAmounts = updatedTimes))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Another Time")
                }

                // Remove entire schedule pattern button
                if (entry != state.scheduleEntries.first()) {
                    OutlinedButton(
                        onClick = { vm.removeTimeEntry(index) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Remove This Pattern")
                    }
                }
            }
        }
    }

    @Composable
    private fun WeekDaySelector(
        selectedDays: Set<Int>,
        onDaySelected: (Int) -> Unit,
    ) {
        // Days labels starting with Monday
        // TODO: Translate this
        // TODO: Change colors
        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        // Day indices now Monday(1) to Sunday(0) - adjusted to start with Monday
        val dayIndices = listOf(1, 2, 3, 4, 5, 6, 0)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayIndices.forEachIndexed { index, day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .clickable { onDaySelected(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = days[index],
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}