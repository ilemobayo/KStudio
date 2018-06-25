package com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.model.Song

@Database(entities = arrayOf(Song::class), version = 1, exportSchema = false)
abstract class SongFavDatabase: RoomDatabase() {
    abstract fun songFavDAO(): SongFavDAO

    companion object {

        private val LOG_TAG = SongFavDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "song"
        private var sInstance: SongFavDatabase? = null

        fun getsInstance(context: Context): SongFavDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, SongFavDatabase::class.java,
                            SongFavDatabase.DATABASE_NAME).allowMainThreadQueries().build()
                }
            }
            return sInstance
        }
    }
}