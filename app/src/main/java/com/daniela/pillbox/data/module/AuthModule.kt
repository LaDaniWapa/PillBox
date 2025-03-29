package com.daniela.pillbox.data.module

import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.viewmodels.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthRepository(androidContext()) }
    viewModel { AuthViewModel(get()) }
}