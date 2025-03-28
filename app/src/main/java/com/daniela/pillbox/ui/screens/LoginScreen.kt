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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.daniela.pillbox.R
import com.daniela.pillbox.ui.components.LabelTextField
import com.daniela.pillbox.ui.components.MyButton

data class LoginScreen(val modifier: Modifier) : Screen {
    @Composable
    override fun Content() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.height(32.dp))

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
                modifier = modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Email TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "example@gmail.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password TextField
                LabelTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Contraseña",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "*****",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                MyButton(
                    onClick = { navigator.replaceAll(HomeScreen(modifier)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log in", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create Account Text
                TextButton(
                    onClick = { navigator.replaceAll(RegisterScreen(modifier)) }) {
                    Text("Aun no tienes cuenta? Crea una", fontSize = 14.sp)
                }
            }
        }
    }
}