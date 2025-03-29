package com.daniela.pillbox.ui.screens

import HomeScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.MyButton
import com.daniela.pillbox.viewmodels.LoginViewModel
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf


data class LoginScreen(val modifier: Modifier) : Screen {
    @Composable
    inline fun <reified T : ScreenModel> rememberVoyagerScreenModel(): T {
        val koin = getKoin()
        val context = LocalContext.current
        return rememberScreenModel {
            koin.get(parameters = { parametersOf(context) })
        }
    }


    @Composable
    override fun Content() {
        val vm = rememberVoyagerScreenModel<LoginViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(vm.loginSuccess) {
            vm.loginSuccess.collect { success ->
                if (success) navigator.replaceAll(HomeScreen(modifier))
            }
        }

        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Logo
            Icon(
                painter = painterResource(R.drawable.pillbox_logo),
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            )

            Column(
                modifier = modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Email TextField
                LabelTextField(
                    value = vm.email,
                    onValueChange = { vm.updateEmail(it) },
                    label = stringResource(R.string.email),
                    placeholder = stringResource(R.string.email_example),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = vm.emailError != null,
                    supportingText = vm.emailError?.let { stringResource(it) }
                )

                Spacer(Modifier.height(16.dp))

                // Password TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.password,
                    onValueChange = { vm.updatePassword(it) },
                    label = stringResource(R.string.password),
                    placeholder = stringResource(R.string.password_example),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { vm.login() }
                    ),
                    isError = vm.passwordError != null,
                    supportingText = vm.passwordError?.let { stringResource(it) }
                )

                vm.apiError?.let { error ->
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                MyButton(
                    onClick = { vm.login() },

                    modifier = Modifier.fillMaxWidth(),
                    enabled = !vm.isLoading
                ) {
                    Text(stringResource(R.string.login), fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create Account Text
                TextButton(
                    onClick = { navigator.replaceAll(RegisterScreen(modifier)) }) {
                    Text(stringResource(R.string.no_account_signup), fontSize = 14.sp)
                }
            }
        }
    }
}