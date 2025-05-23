package com.daniela.pillbox.data.module

import com.daniela.pillbox.utils.AlarmScheduler
import com.daniela.pillbox.viewmodels.AlarmViewModel
import org.koin.dsl.module

/**
 * Koin module for handling the alarm operations.
 */
val alarmModule = module {
    single { AlarmScheduler(get()) }
    factory { AlarmViewModel(get(), get(), get()) }
}
