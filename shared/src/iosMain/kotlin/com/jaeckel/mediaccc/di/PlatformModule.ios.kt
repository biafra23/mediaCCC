package com.jaeckel.mediaccc.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jaeckel.mediaccc.data.db.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun platformModule(): Module = module {
    single<AppDatabase> {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = (documentDirectory?.path ?: NSHomeDirectory()) + "/${AppDatabase.DATABASE_NAME}"
        
        Room.databaseBuilder<AppDatabase>(
            name = path
        ).setDriver(BundledSQLiteDriver()).build()
    }
}
