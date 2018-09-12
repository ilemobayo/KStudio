package com.musicplayer.aow.delegates.data.db.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.db.dao.ArtistDAO
import com.musicplayer.aow.delegates.data.model.Artists

@Database(entities = arrayOf(Artists::class), version = 1, exportSchema = false)
abstract class ArtistDatabase: RoomDatabase() {
    abstract fun artistDAO(): ArtistDAO

    companion object {

        private val LOG_TAG = ArtistDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "artist"
        private var sInstance: ArtistDatabase? = null

        fun getsInstance(context: Context): ArtistDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, ArtistDatabase::class.java,
                            ArtistDatabase.DATABASE_NAME).build()
                }
            }
            return sInstance
        }
    }
}