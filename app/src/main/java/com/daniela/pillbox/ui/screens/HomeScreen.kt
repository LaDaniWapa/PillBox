package com.daniela.pillbox.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import io.appwrite.models.User

data class HomeScreen(
    val modifier: Modifier = Modifier.fillMaxSize(),
    val user: User<Map<String, Any>>,
) : Screen {
    @Composable
    override fun Content() {
        Column(modifier = modifier) {
            Text(user.name)
        }
    }

}