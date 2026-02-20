package com.jaeckel.mediaccc.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlaybackHistoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        const val DATABASE_NAME = "mediaccc.db"
    }
}
