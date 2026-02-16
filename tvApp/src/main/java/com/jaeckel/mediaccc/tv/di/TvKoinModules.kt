package com.jaeckel.mediaccc.tv.di

import com.jaeckel.mediaccc.di.sharedModule
import org.koin.dsl.module

/**
 * Koin module for TV app specific dependencies.
 */
val tvViewModelModule = module {
    // ViewModels are now in shared module
}

val tvAppModules = listOf(sharedModule, tvViewModelModule)



