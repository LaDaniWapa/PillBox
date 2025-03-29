package com.daniela.pillbox.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Locale

@Composable
fun LabelTextField(
    modifier: Modifier = Modifier,
    label: String = "Label",
    value: String = "Text",
    placeholder: String? = "Placeholder",
    onValueChange: (String) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    supportingText: String? = null
) {
    Column {
        //External label
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.primary
        )

        // TextField
        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            label = null,
            placeholder = {
                if (placeholder != null)
                    Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            isError = isError,
            supportingText = {
                if (supportingText != null)
                    Text(text = supportingText)
            },

            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
    }
}

@Composable
@Preview(showBackground = true)
fun Title(modifier: Modifier = Modifier, text: String = "Text") {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
@Preview(showBackground = true)
fun Label(modifier: Modifier = Modifier, text: String = "Text") {
    Text(
        modifier = modifier,
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
@Preview(showBackground = true)
fun MyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable (RowScope.() -> Unit) = { Text("Button") },
) {
    Button(
        onClick = onClick,
        content = content,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}


@Composable
@Preview(showBackground = true)
fun SelectableList(
    list: List<String> = listOf("Item 1", "Item 2", "Item 3"),
    onClick: (Int) -> Unit = {},
) {
    var localSelectedIndex by remember { mutableIntStateOf(-1) }

    Surface(
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        LazyColumn(
            modifier = Modifier.padding(5.dp)
        ) {
            itemsIndexed(items = list) { index, item ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = localSelectedIndex == index,
                            onClick = {
                                localSelectedIndex = index
                                onClick(index)
                            },
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = item,
                    )

                    if (localSelectedIndex == index) Icon(
                        contentDescription = "check",
                        imageVector = Icons.Default.Check,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (index != list.size - 1) HorizontalDivider(
                    Modifier.fillMaxWidth(), thickness = 1.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit = {},
    btnList: List<String> = listOf("Option 1", "Option 2", "Option 3"),
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        btnList.forEachIndexed { i, opt ->
            SegmentedButton(
                selected = selectedIndex == i,
                onClick = {
                    selectedIndex = i
                    onSelected(i)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = i, count = btnList.size
                ),
                icon = {}
            ) {
                Text(
                    text = opt,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (selectedIndex == i) TextDecoration.Underline else TextDecoration.None,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun DropDownMenu(
    modifier: Modifier = Modifier,
    list: List<String> = listOf("Item 1", "Item 2", "Item 3"),
    onSelected: (Int) -> Unit = {},
    initialState: Boolean = false,
    label: String = "Choose an option",
) {
    var expanded by remember { mutableStateOf(initialState) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = list[selectedIndex],
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            list.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        selectedIndex = list.indexOf(item)
                        expanded = false
                        onSelected(selectedIndex)
                    },
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun FloatInputField(
    modifier: Modifier = Modifier,
    value: String = "3.3",
    onValueChange: (String) -> Unit = {},
    label: String = "",
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Allow only valid float inputs
            if (input.isEmpty() || input.matches(Regex("^-?\\d*\\.?\\d*\$"))) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

// Create an IntInputField where only whole numbers can be inputted by the user
@Composable
@Preview(showBackground = true)
fun IntInputField(
    modifier: Modifier = Modifier,
    value: String = "4",
    onValueChange: (String) -> Unit = {},
    label: String = "",
    suffix: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Allow only valid integer inputs
            if (input.isEmpty() || input.matches(Regex("^-?\\d+"))) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = suffix,

        )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    visibleState: MutableState<Boolean>,
    timePickerState: TimePickerState,
    onTimeSelected: (Int, Int) -> Unit,
    label: String = "Select Time",
) {
    if (visibleState.value) {
        Dialog(
            onDismissRequest = { visibleState.value = false },
            content = {
                Box(
                    modifier = Modifier
                        .clickable { visibleState.value = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .wrapContentHeight(),
                        elevation = CardDefaults.cardElevation(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TimePicker(
                                state = timePickerState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(onClick = { visibleState.value = false }) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = { visibleState.value = false }) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    TextButton(onClick = {
                                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                                        visibleState.value = false
                                    }) {
                                        Text("OK", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun TimePickerButton(
    modifier: Modifier = Modifier,
    label: String = "Select Time",
    onTimeSelected: (Int, Int) -> Unit = { _, _ -> },
) {
    val visibleState = remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Surface(
        modifier = modifier,
        shape = CardDefaults.shape,
        tonalElevation = 32.dp,
        onClick = {
            visibleState.value = true
        },

        ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    timePickerState.hour,
                    timePickerState.minute
                ),
                modifier = Modifier
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    CustomTimePickerDialog(
        visibleState = visibleState,
        timePickerState = timePickerState,
        onTimeSelected = onTimeSelected,
        label = label
    )
}