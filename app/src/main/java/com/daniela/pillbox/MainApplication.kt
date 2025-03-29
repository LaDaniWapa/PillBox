package com.daniela.pillbox

import android.app.Application
import com.daniela.pillbox.data.module.authModule
import com.daniela.pillbox.data.module.splashModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(
                authModule, splashModule
            )
        }
    }
}