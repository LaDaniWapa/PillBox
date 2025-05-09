package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Title
                Text(
                    text = "New Medication",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
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
                    label = "Medication Name",
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
                        label = "Dosage",
                        value = vm.dosage,
                        onValueChange = { vm.onDosageChange(it) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                        ),
                    )

                    LabelTextField(
                        modifier = Modifier.weight(1f),
                        label = "Unit",
                        value = vm.dosageUnit,
                        onValueChange = { vm.onDosageUnitChange(it) },
                    )
                }

                // Medication Type Dropdown
                DropDownMenu(
                    list = vm.medicationTypes,
                    label = "Type",
                    onSelected = { vm.onTypeChange(vm.medicationTypes[it]) },
                )

                // Stock Quantity
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.stock,
                    onValueChange = { vm.onStockChange(it) },
                    label = "Stock Quantity",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                // Instructions
                LabelTextField(
                    value = vm.instructions,
                    onValueChange = { vm.onInstructionsChange(it) },
                    label = "Instructions",
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
                    label = "Notes",
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
                    Text("Save Medication")
                }
            }
        }
    }
}
