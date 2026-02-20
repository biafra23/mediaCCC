package com.jaeckel.mediaccc.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jaeckel.mediaccc.data.db.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

actual fun platformModule(): Module = module {
    single<AppDatabase> {
        Room.databaseBuilder<AppDatabase>(
            name = NSHomeDirectory() + "/${AppDatabase.DATABASE_NAME}"
        ).setDriver(BundledSQLiteDriver()).build()
    }
}
