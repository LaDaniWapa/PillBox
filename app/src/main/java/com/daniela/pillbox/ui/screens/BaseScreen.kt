package com.daniela.pillbox.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

/**
 * Base class for all screens in the application.
 */
abstract class BaseScreen : Screen {
    /**
     * Creates and remembers a [ScreenModel] instance.
     */
    @Composable
    inline fun <reified T : ScreenModel> rememberVoyagerScreenModel(): T {
        val koin = getKoin()
        val context = LocalContext.current
        return rememberScreenModel {
            koin.get(parameters = { parametersOf(context) })
        }
    }

    /**
     * Creates and remembers a [ScreenModel] instance with parameters.
     */
    @Composable
    inline fun <reified T : ScreenModel> rememberVoyagerScreenModel(vararg params: Any?): T {
        val koin = getKoin()
        val context = LocalContext.current
        return rememberScreenModel {
            koin.get(parameters = { parametersOf(context, *params) })
        }
    }

    /**
     * Content of the screen
     */
    @Composable
    abstract override fun Content()
}
