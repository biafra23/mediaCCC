package com.jaeckel.mediaccc.di

import androidx.room.Room
import com.jaeckel.mediaccc.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AppDatabase> {
        val ctx = androidContext()
        Room.databaseBuilder<AppDatabase>(
            context = ctx,
            name = ctx.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath
        ).build()
    }
}
