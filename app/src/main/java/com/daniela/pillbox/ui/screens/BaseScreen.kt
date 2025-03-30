package com.daniela.pillbox.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

abstract class BaseScreen : Screen {
    @Composable
    inline fun <reified T : ScreenModel> rememberVoyagerScreenModel(): T {
        val koin = getKoin()
        val context = LocalContext.current
        return rememberScreenModel {
            koin.get(parameters = { parametersOf(context) })
        }
    }

    @Composable
    abstract override fun Content()
}