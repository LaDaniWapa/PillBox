package com.daniela.pillbox.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import com.daniela.pillbox.ui.screens.HomeScreen
import com.daniela.pillbox.ui.theme.AppTheme

/**
 * Main activity for the app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Splash screen
        installSplashScreen().apply {
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
                /**
                 * Content holder, this add needed padding values everywhere in the app
                 */
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                    ) {
                        Navigator(HomeScreen())
                    }
                }
            }
        }
    }
}
