package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.ui.components.MedicationItem
import com.daniela.pillbox.viewmodels.HomeViewModel
import io.appwrite.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeScreen(
    val modifier: Modifier = Modifier.fillMaxSize(),
    val user: User<Map<String, Any>>,
) : BaseScreen() {
    @Composable
    override fun Content() {
        val vm = rememberVoyagerScreenModel<HomeViewModel>(user)
        //val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = modifier.padding(horizontal = 16.dp),
        ) {
            // Header with date and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = vm.getGreeting(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box {
                    IconButton(onClick = { vm.showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = vm.showMenu,
                        onDismissRequest = { vm.showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Storage") },
                            onClick = { /*navigator.push(StorageScreen())*/ }
                        )
                        DropdownMenuItem(
                            text = { Text("Reload") },
                            onClick = { /*navigator.push(StorageScreen())*/ }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = { /* Handle logout */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's medications
            Text(
                text = "Today's Medications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            vm.medications.forEachIndexed { index, medication ->
                MedicationItem(
                    medication = medication,
                    isChecked = vm.checkedStates[index] == true,
                    onCheckedChange = { vm.checkedStates[index] = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}