package com.daniela.pillbox

import android.app.Application
import com.daniela.pillbox.data.module.alarmModule
import com.daniela.pillbox.data.module.authModule
import com.daniela.pillbox.data.module.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * @suppress
 * Main application class.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                authModule, storageModule, alarmModule
            )
        }
    }
}
