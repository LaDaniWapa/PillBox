package com.daniela.pillbox.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import com.daniela.pillbox.ui.screens.LoginScreen
import com.daniela.pillbox.ui.theme.AppTheme
import com.daniela.pillbox.viewmodels.AuthViewModel
import com.daniela.pillbox.viewmodels.SplashViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val authVM: AuthViewModel by viewModel()
    private val splashVM: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashVM.isReady.value != true
            }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, .6f, .0f)
                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, .6f, .0f)

                zoomX.duration = 400L
                zoomX.doOnEnd { screen.remove() }
                zoomY.duration = 400L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Navigator(LoginScreen(modifier = Modifier.Companion.padding(innerPadding)))
                }
            }
        }
    }
}