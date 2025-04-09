package com.daniela.pillbox.data.module

import androidx.lifecycle.SavedStateHandle
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.viewmodels.StorageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import android.content.Context

val storageModule = module {
    single { AuthRepository(androidContext()) }
    single { MedicationRepository() }

    factory { (ctx: Context, savedStateHandle: SavedStateHandle) ->
        StorageViewModel(
            savedStateHandle = savedStateHandle,
            authRepository = get(),
            medsRepository = get(),
            ctx = ctx
        )
    }
}