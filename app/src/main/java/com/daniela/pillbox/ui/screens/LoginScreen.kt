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
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.MyButton
import com.daniela.pillbox.viewmodels.LoginViewModel

/**
 * The login screen where users can sign in into their account.
 */
class LoginScreen : BaseScreen() {
    @Composable
    override fun Content() {
        val vm = rememberVoyagerScreenModel<LoginViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(vm.loginSuccess) {
            vm.loginSuccess.collect { success ->
                if (success) {
                    vm.getLoggedInUser().let { user ->
                        navigator.replaceAll(HomeScreen())
                    }
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

            // Form
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(32.dp))

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

                Spacer(Modifier.height(16.dp))

                // Password TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.password),
                    value = vm.password,
                    placeholder = stringResource(R.string.password_example),
                    onValueChange = { vm.updatePassword(it) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = vm.passwordError != null,
                    supportingText = vm.passwordError?.let { stringResource(it) },
                    keyboardActions = KeyboardActions(
                        onDone = { vm.login() }
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
                    onClick = { navigator.replaceAll(RegisterScreen()) }) {
                    Text(stringResource(R.string.no_account_signup), fontSize = 14.sp)
                }
            }
        }
    }
}
