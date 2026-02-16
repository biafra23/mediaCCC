package com.jaeckel.mediaccc

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import com.jaeckel.mediaccc.di.sharedModule
import com.jaeckel.mediaccc.ui.navigation.AppNavHost
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}

fun MainViewController() = ComposeUIViewController {
    MaterialTheme {
        AppNavHost()
    }
}
