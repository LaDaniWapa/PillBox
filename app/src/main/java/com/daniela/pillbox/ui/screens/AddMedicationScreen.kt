package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.MedicationWithDocId
import com.daniela.pillbox.libs.colorpicker.ColorPicker
import com.daniela.pillbox.libs.colorpicker.ColorPickerType
import com.daniela.pillbox.ui.components.DropDownMenu
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.MyButton
import com.daniela.pillbox.viewmodels.AddMedicationViewModel

/**
 * Screen for adding a new medication.
 */
class AddMedicationScreen(private val medicationToEdit: MedicationWithDocId? = null) : BaseScreen() {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val ssh = SavedStateHandle()
        val vm = rememberVoyagerScreenModel<AddMedicationViewModel>(ssh, medicationToEdit)

        val scrollState = rememberScrollState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(vm.success) {
            vm.success.collect { success ->
                if (success) {
                    navigator.pop()
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ActionBar and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                    text = vm.title.value,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
            }

            // Form content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
            ) {
                // Medication Name
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.medication_name),
                    value = vm.name,
                    onValueChange = { vm.onNameChange(it) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                // Dosage Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabelTextField(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.dosage),
                        value = vm.dosage,
                        onValueChange = { vm.onDosageChange(it) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                        ),
                    )

                    LabelTextField(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.unit),
                        value = vm.dosageUnit,
                        onValueChange = { vm.onDosageUnitChange(it) },
                    )
                }

                // Medication Type Dropdown
                DropDownMenu(
                    list = vm.medicationTypes,
                    label = stringResource(R.string.type),
                    onSelected = { vm.onTypeChange(vm.medicationTypes[it]) },
                )

                // Stock Quantity
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.stock,
                    onValueChange = { vm.onStockChange(it) },
                    label = stringResource(R.string.stock_quantity),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                // Instructions
                LabelTextField(
                    value = vm.instructions,
                    onValueChange = { vm.onInstructionsChange(it) },
                    label = stringResource(R.string.instructions),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // Notes
                LabelTextField(
                    value = vm.notes,
                    onValueChange = { vm.onNotesChange(it) },
                    label = stringResource(R.string.notes),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // Color Picker
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ColorPicker(
                        onPickedColor = vm::onColorChange,
                        type = ColorPickerType.Ring(
                            showAlphaBar = false,
                            initialColor = Color(vm.color.toColorInt()),
                            ringWidth = 20.dp
                        ),
                    )
                }

                // Save Button
                MyButton(
                    onClick = vm::onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = true
                ) {
                    Text(stringResource(R.string.save_medication))
                }
            }
        }
    }
}
