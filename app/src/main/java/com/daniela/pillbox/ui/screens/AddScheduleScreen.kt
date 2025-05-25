package com.daniela.pillbox.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.ScheduleWithDocId
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.TimePickerButton
import com.daniela.pillbox.viewmodels.AddScheduleViewModel

class AddScheduleScreen(
    private val medicationId: String,
    private val schedulesToEdit: List<ScheduleWithDocId>? = null,
) : BaseScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberVoyagerScreenModel<AddScheduleViewModel>(medicationId, schedulesToEdit)
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
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = state.title,
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
                state.schedules.forEachIndexed { index, schedule ->
                    ScheduleEntryItem(
                        schedule = schedule,
                        state = state,
                        index = index,
                        vm = vm
                    )
                }

                if (schedulesToEdit == null)
                    // Add new schedule pattern button
                    Button(
                        onClick = vm::addSchedule,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_another_schedule_pattern))
                    }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { }) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { vm.saveSchedule(); navigator.pop() }
                ) {
                    Text(stringResource(R.string.save_schedule))
                }
            }
        }
    }

    @Composable
    private fun ScheduleEntryItem(
        vm: AddScheduleViewModel,
        state: AddScheduleViewModel.AddScheduleUiState,
        schedule: ScheduleWithDocId,
        index: Int,
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.on_these_days))
                // Days selection
                val days = schedule.weekDays ?: emptyList()
                WeekDaySelector(
                    selectedDays = days,
                    onDaySelected = { day ->
                        val newDays = if (days.contains(day)) {
                            days - day
                        } else {
                            days + day
                        }
                        vm.updateSchedule(index, schedule.copy(weekDays = newDays))
                    }
                )

                // Times with amounts
                val timesWithAmounts =
                    (schedule.times ?: listOf("00:00")).zip((schedule.amounts ?: listOf(1)))

                timesWithAmounts.forEachIndexed { timeIndex, (time, amount) ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Time input
                        TimePickerButton(
                            onTimeSelected = { hour, min ->
                                val newTime = String.format(null, "%02d:%02d", hour, min)
                                val updatedTimes = timesWithAmounts.toMutableList().apply {
                                    set(timeIndex, newTime to amount)
                                }
                                vm.updateSchedule(
                                    index, schedule.copy(
                                        times = updatedTimes.map { it.first },
                                    )
                                )
                            },
                            initialTime = time.split(":").map { it.toInt() }
                        )

                        // Amount input
                        LabelTextField(
                            modifier = Modifier.width(80.dp),
                            label = stringResource(R.string.amount),
                            value = amount.toString(),
                            onValueChange = { newAmount ->
                                val updatedTimes =
                                    timesWithAmounts.toMutableList().apply {
                                        set(timeIndex, time to (newAmount.toIntOrNull() ?: 1))
                                    }
                                vm.updateSchedule(
                                    index, schedule.copy(
                                        amounts = updatedTimes.map { it.second }
                                    ))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Remove time button
                        if (timeIndex > 0) {
                            IconButton(onClick = {
                                val updatedTimes =
                                    timesWithAmounts.toMutableList().apply {
                                        removeAt(timeIndex)
                                    }
                                vm.updateSchedule(
                                    index, schedule.copy(
                                        times = updatedTimes.map { it.first },
                                    )
                                )
                            }) {
                                Icon(Icons.Rounded.Close, stringResource(R.string.remove_time))
                            }
                        }
                    }
                }

                // Add time button
                Button(
                    onClick = {
                        val updatedTimes = timesWithAmounts + ("00:00" to 1)
                        Log.i("TAG", "ScheduleEntryItem: $updatedTimes")
                        vm.updateSchedule(
                            index, schedule.copy(
                                times = updatedTimes.map { it.first },
                                amounts = updatedTimes.map { it.second }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add_another_time))
                }


                // Remove entire schedule pattern button
                if (schedule !== state.schedules.first()) {
                    OutlinedButton(
                        onClick = { vm.removeSchedule(index) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.remove_this_pattern))
                    }
                }
            }
        }
    }

    @Composable
    private fun WeekDaySelector(
        selectedDays: List<Int>,
        onDaySelected: (Int) -> Unit,
    ) {
        // Days labels starting with Monday
        // TODO: Translate this
        // TODO: Change colors
        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        // Day indices now Monday(0) to Sunday(6) - adjusted to start with Monday
        val dayIndices = listOf(0, 1, 2, 3, 4, 5, 6)

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