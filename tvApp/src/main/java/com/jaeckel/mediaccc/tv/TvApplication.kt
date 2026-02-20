package com.jaeckel.mediaccc.tv

import android.app.Application
import com.jaeckel.mediaccc.di.platformModule
import com.jaeckel.mediaccc.tv.di.tvAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TvApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TvApplication)
            modules(tvAppModules + platformModule())
        }
    }
}




