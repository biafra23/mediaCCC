package com.jaeckel.mediaccc

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.svg.SvgDecoder
import com.jaeckel.mediaccc.di.platformModule
import com.jaeckel.mediaccc.di.sharedModule
import com.jaeckel.mediaccc.ui.navigation.AppNavHost
import org.koin.core.context.startKoin
import platform.Foundation.NSBundle

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

    val info = NSBundle.mainBundle.infoDictionary
    val versionName = info?.get("CFBundleShortVersionString") as? String ?: ""
    val buildNumber = info?.get("CFBundleVersion") as? String ?: ""
    val bundleId = info?.get("CFBundleIdentifier") as? String ?: ""
    val gitHash = info?.get("GitCommitHash") as? String ?: ""
    val versionString = buildString {
        append("$bundleId $versionName ($buildNumber)")
        if (gitHash.isNotEmpty() && gitHash != "unknown") append(" [$gitHash]")
    }

    MaterialTheme {
        AppNavHost(versionString = versionString)
    }
}
