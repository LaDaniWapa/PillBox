package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.ui.components.FullScreenLoader
import com.daniela.pillbox.viewmodels.HomeViewModel
import com.daniela.pillbox.viewmodels.HomeViewModel.AuthState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt
import com.daniela.pillbox.data.models.ScheduleWithMedicationAndDocId
import com.daniela.pillbox.utils.capitalized


/**
 * The main screen of the application, displaying the user's medications for the current day.
 */
class HomeScreen : BaseScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberVoyagerScreenModel<HomeViewModel>()
        val state by vm.uiState

        when (val authState = state.authState) {
            is AuthState.Loading -> FullScreenLoader()
            is AuthState.Authenticated -> MainContent(vm, navigator, state)
            is AuthState.Unauthenticated -> navigator.replaceAll(LoginScreen())
            is AuthState.Error -> ErrorScreen(authState.errorMessage) {}
        }
    }

    /**
     * Displays an error message and a retry button.
     * @param message The error message to display.
     * @param onRetry A function to be called when the retry button is clicked.
     */
    @Composable
    private fun ErrorScreen(
        message: String?,
        onRetry: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Error icon
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error title
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Error message (if available)
            message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Retry button
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }

    /**
     * Displays the main content of the HomeScreen, including the header, date, greeting, and medication list.
     *
     * @param vm The HomeViewModel instance.
     * @param navigator The Voyager navigator for handling screen transitions.
     * @param state The current state of the HomeViewModel.
     */
    @Composable
    fun MainContent(vm: HomeViewModel, navigator: Navigator, state: HomeViewModel.HomeUiState) {
        // Header with date and menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header
            Column {
                // Date
                Text(
                    text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Greeting
                Text(
                    text = vm.getGreeting(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Options Menu
            Box {
                IconButton(onClick = { vm.toggleMenu() }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu))
                }
                DropdownMenu(
                    expanded = state.showMenu,
                    onDismissRequest = { vm.toggleMenu() }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.storage)) },
                        onClick = { navigator.push(StorageScreen()) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.reload)) },
                        onClick = { /*navigator.push(StorageScreen())*/ }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.logout)) },
                        onClick = { vm.logout() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's medications
        Text(
            text = stringResource(R.string.today_s_medications),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Scrollable Medication List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = {
                        vm.testAlarmSystem()
                    }
                ) {
                    Text(stringResource(R.string.add_alarm))
                }
            }

            items(state.schedulesWithMedications) { med ->
                NewMedicationItem(
                    scheduleWithMedication = med,
                    isChecked = vm.isMedicationTaken(med.docId!!, med.times?.get(0)!!),
                    onCheckedChange = { vm.toggleMedicationChecked(med) }
                )
            }
        }
    }
}

@Composable
private fun DotSeparator() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    )
}

@Composable
private fun NewMedicationItem(
    modifier: Modifier = Modifier,
    scheduleWithMedication: ScheduleWithMedicationAndDocId,
    onCheckedChange: () -> Unit,
    isChecked: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onCheckedChange,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time & Icon Column
            Column(
                modifier = Modifier.padding(end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scheduleWithMedication.times?.get(0) ?: "??:??",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Medication icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = scheduleWithMedication.medicationObj?.color?.let { Color(it.toColorInt()) }
                                ?: MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
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
            }

            // Medication Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                // Name
                Text(
                    text = scheduleWithMedication.medicationObj?.name ?: "Unknown Medication",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Name
                Text(
                    text = scheduleWithMedication.userId ?: "null",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dosage and type
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buildString {
                            append(scheduleWithMedication.amounts?.get(0) ?: "")
                            append(scheduleWithMedication.medicationObj?.dosageUnit ?: "")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    DotSeparator()

                    Text(
                        text = (scheduleWithMedication.medicationObj?.type ?: "").capitalized(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Instructions
                scheduleWithMedication.medicationObj?.instructions?.let { instructions ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}