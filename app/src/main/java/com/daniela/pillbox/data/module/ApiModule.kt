package com.daniela.pillbox.data.module

import android.content.Context
import com.daniela.pillbox.data.repository.ApiRepository
import com.daniela.pillbox.viewmodels.ApiDetailsViewModel
import com.daniela.pillbox.viewmodels.ApiSearchViewModel
import org.koin.dsl.module

val apiModule = module {
    single { ApiRepository() }

    factory { (ctx: Context) ->
        ApiSearchViewModel(apiRepository = get(), ctx = ctx)
    }

    factory { (ctx: Context, nregistro: String) ->
        ApiDetailsViewModel(apiRepository = get(), nregistro = nregistro, ctx = ctx)
    }

}