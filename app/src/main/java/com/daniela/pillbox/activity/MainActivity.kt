package com.daniela.pillbox.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.app.ComponentCaller
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import com.daniela.pillbox.ui.screens.HomeScreen
import com.daniela.pillbox.ui.theme.AppTheme

/**
 * Main activity for the app
 */
class MainActivity : ComponentActivity() {
    private val NOTIFICATION_PERMISSION_CODE = 1001
    private val EXACT_ALARM_PERMISSION_CODE = 1002

    private var pendingPermissionAction: (() -> Unit)? = null
    private var pendingDeniedAction: (() -> Unit)? = null

    fun checkAndRequestPermissions(action: () -> Unit, onDenied: (() -> Unit)? = null): Boolean {
        // Store callbacks
        pendingPermissionAction = action
        pendingDeniedAction = onDenied

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
            return false
        }

        // Check exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    startActivityForResult(
                        Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
                        EXACT_ALARM_PERMISSION_CODE
                    )
                } catch (e: ActivityNotFoundException) {
                    showPermissionFallbackDialog()
                }
                return false
            }
        }

        // All permissions granted - execute immediately
        action.invoke()
        clearCallbacks()
        return true
    }

    private fun showPermissionFallbackDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Please enable 'Allow exact alarms' in system settings")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .setNegativeButton("Cancel") { _, _ ->
                pendingDeniedAction?.invoke()
                clearCallbacks()
            }
            .setOnDismissListener {
                pendingDeniedAction?.invoke()
                clearCallbacks()
            }
            .show()
    }

    private fun clearCallbacks() {
        pendingPermissionAction = null
        pendingDeniedAction = null
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted, now check exact alarms
                checkAndRequestPermissions(
                    action = { pendingPermissionAction?.invoke() },
                    onDenied = pendingDeniedAction
                )
            } else {
                pendingDeniedAction?.invoke()
                clearCallbacks()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller,
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        if (requestCode == EXACT_ALARM_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(AlarmManager::class.java)
                if (alarmManager.canScheduleExactAlarms()) {
                    pendingPermissionAction?.invoke()
                } else {
                    pendingDeniedAction?.invoke()
                }
                clearCallbacks()
            }
        }
    }


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
