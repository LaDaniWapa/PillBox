package com.daniela.pillbox.data.module

import android.content.Context
import com.daniela.pillbox.data.repository.AuthRepository
import com.daniela.pillbox.utils.Helper
import com.daniela.pillbox.viewmodels.AuthViewModel
import com.daniela.pillbox.viewmodels.HomeViewModel
import com.daniela.pillbox.viewmodels.LoginViewModel
import com.daniela.pillbox.viewmodels.RegisterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for handling the authentication operations.
 */
val authModule = module {
    single { AuthRepository(androidContext()) }
    single { Helper(androidContext()) }
    viewModel { AuthViewModel(get()) }

    factory { (ctx: Context) ->
        RegisterViewModel(authRepository = get(), authErrorHandler = get(), ctx = ctx)
    }

    factory { (ctx: Context) ->
        LoginViewModel(authRepository = get(), authErrorHandler = get(), ctx = ctx)
    }

    factory { (ctx: Context) ->
        HomeViewModel(authRepository = get(), ctx = ctx)
    }
}
