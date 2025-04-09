package com.daniela.pillbox.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.ui.components.FullScreenLoader
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.MyButton
import com.daniela.pillbox.viewmodels.RegisterViewModel

class RegisterScreen : BaseScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm: RegisterViewModel = rememberVoyagerScreenModel<RegisterViewModel>()

        if (vm.isLoading) {
            FullScreenLoader()
        }

        LaunchedEffect(vm.registerSuccess) {
            vm.registerSuccess.collect { success ->
                if (success) {
                    navigator.replaceAll(HomeScreen())
                }
            }
        }

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
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
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Name TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.name),
                    value = vm.name,
                    placeholder = stringResource(R.string.name_example),
                    onValueChange = { vm.updateName(it) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    isError = vm.nameError != null,
                    supportingText = vm.nameError?.let { stringResource(it) },

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.email),
                    value = vm.email,
                    placeholder = stringResource(R.string.email_example),
                    onValueChange = { vm.updateEmail(it) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = vm.emailError != null,
                    supportingText = vm.emailError?.let { stringResource(it) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.password),
                    value = vm.password,
                    placeholder = stringResource(R.string.password_example),
                    onValueChange = { vm.updatePassword(it) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = vm.passwordError != null,
                    supportingText = vm.passwordError?.let { stringResource(it) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.repeat_password),
                    value = vm.confirmPassword,
                    placeholder = stringResource(R.string.password_example),
                    onValueChange = { vm.updateConfirmPassword(it) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = vm.confirmPasswordError != null,
                    supportingText = vm.confirmPasswordError?.let { stringResource(it) },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            vm.register()
                        }
                    ),
                )

                vm.apiError?.let { error ->
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Up Button
                MyButton(
                    onClick = { vm.register() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.signup), fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login TextButton
                TextButton(
                    onClick = { navigator.replaceAll(LoginScreen()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.already_have_account_login),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}