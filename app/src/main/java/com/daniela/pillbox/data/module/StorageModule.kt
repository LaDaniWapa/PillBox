package com.daniela.pillbox.data.module

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.daniela.pillbox.data.repository.MedicationRepository
import com.daniela.pillbox.viewmodels.AddMedicationViewModel
import com.daniela.pillbox.viewmodels.StorageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for handling the db crud operations.
 */
val storageModule = module {
    single { MedicationRepository(androidContext()) }

    factory { (ctx: Context, savedStateHandle: SavedStateHandle) ->
        StorageViewModel(
            savedStateHandle = savedStateHandle,
            authRepository = get(),
            medsRepository = get(),
            ctx = ctx
        )
    }

    factory { (ctx: Context, savedStateHandle: SavedStateHandle) ->
        AddMedicationViewModel(
            authRepository = get(),
            medsRepository = get(),
            savedStateHandle = savedStateHandle,
            ctx = ctx
        )
    }
}
