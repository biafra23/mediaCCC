package com.jaeckel.mediaccc.tv.di

import com.jaeckel.mediaccc.di.sharedModule
import com.jaeckel.mediaccc.tv.viewmodel.TvHomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for TV app specific dependencies.
 */
val tvViewModelModule = module {
    viewModel { TvHomeViewModel(get()) }
}

val tvAppModules = listOf(sharedModule, tvViewModelModule)



