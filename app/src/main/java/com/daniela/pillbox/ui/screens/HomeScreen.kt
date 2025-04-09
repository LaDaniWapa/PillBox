package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.ui.components.FullScreenLoader
import com.daniela.pillbox.ui.components.MedicationItem
import com.daniela.pillbox.viewmodels.HomeViewModel
import com.daniela.pillbox.viewmodels.HomeViewModel.AuthState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeScreen : BaseScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = rememberVoyagerScreenModel<HomeViewModel>()
        val authState = vm.authState.collectAsState()

        LaunchedEffect(authState) {
            vm.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {} /* Show loading if needed */
                    is AuthState.Authenticated -> {} /* User is logged in */
                    is AuthState.Unauthenticated -> navigator.replaceAll(LoginScreen())
                }
            }
        }

        if (vm.isLoading)
            FullScreenLoader()
        else
            MainContent(vm, navigator)
    }

    @Composable
    fun MainContent(vm: HomeViewModel, navigator: Navigator) {
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
                        onClick = { navigator.push(StorageScreen()) }
                    )
                    DropdownMenuItem(
                        text = { Text("Reload") },
                        onClick = { /*navigator.push(StorageScreen())*/ }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = { vm.logout() }
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

        // Medication List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vm.medications) { med ->
                MedicationItem(
                    medication = med,
                    isChecked = vm.isMedicationTaken(med.name),
                    onCheckedChange = { vm.toggleMedication(med.name) }

                )
            }
        }
    }
}