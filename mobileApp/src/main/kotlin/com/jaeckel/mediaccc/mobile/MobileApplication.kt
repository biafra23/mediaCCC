package com.jaeckel.mediaccc.mobile

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.google.android.gms.cast.framework.CastContext
import com.jaeckel.mediaccc.di.platformModule
import com.jaeckel.mediaccc.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MobileApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MobileApplication)
            modules(sharedModule, platformModule())
        }

        // Initialise CastContext eagerly so the Cast button is ready when screens load
        runCatching { CastContext.getSharedInstance(this) }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}






