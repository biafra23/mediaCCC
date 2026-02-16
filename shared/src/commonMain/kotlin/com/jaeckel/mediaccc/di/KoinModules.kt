package com.jaeckel.mediaccc.di

import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.MediaCCCApi
import com.jaeckel.mediaccc.viewmodel.EventDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for shared dependencies.
 *
 * Use Koin's verify() function in unit tests to catch
 * missing dependencies at build time instead of runtime.
 */
val sharedModule = module {
    single { MediaCCCApi() }
    single { MediaRepository(get()) }
    viewModel { (eventGuid: String) -> EventDetailViewModel(get(), eventGuid) }
}



