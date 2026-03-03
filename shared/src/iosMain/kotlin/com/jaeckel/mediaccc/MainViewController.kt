package com.jaeckel.mediaccc

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.svg.SvgDecoder
import com.jaeckel.mediaccc.di.platformModule
import com.jaeckel.mediaccc.di.sharedModule
import com.jaeckel.mediaccc.ui.cast.CastButton
import com.jaeckel.mediaccc.ui.navigation.AppNavHost
import org.koin.core.context.startKoin

fun doInitKoin() {
    startKoin {
        modules(sharedModule, platformModule())
    }
}

fun MainViewController() = ComposeUIViewController {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    MaterialTheme {
        AppNavHost(
            eventDetailExtraActions = { recordingUrl, mimeType, title ->
                CastButton(recordingUrl = recordingUrl, mimeType = mimeType, title = title)
            }
        )
    }
}
