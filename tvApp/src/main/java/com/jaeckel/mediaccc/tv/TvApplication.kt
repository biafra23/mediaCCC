package com.jaeckel.mediaccc.tv

import android.app.Application
import com.jaeckel.mediaccc.tv.di.tvAppModules
import org.koin.core.context.startKoin

class TvApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(tvAppModules)
        }
    }
}




