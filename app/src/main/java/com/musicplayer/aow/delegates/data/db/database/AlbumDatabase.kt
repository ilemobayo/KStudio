package com.musicplayer.aow.delegates.data.db.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.db.dao.AlbumDAO
import com.musicplayer.aow.delegates.data.model.Album

@Database(entities = arrayOf(Album::class), version = 1, exportSchema = false)
abstract class AlbumDatabase: RoomDatabase() {
    abstract fun albumDAO(): AlbumDAO

    companion object {

        private val LOG_TAG = AlbumDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "album"
        private var sInstance: AlbumDatabase? = null

        fun getsInstance(context: Context): AlbumDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, AlbumDatabase::class.java,
                            AlbumDatabase.DATABASE_NAME).build()
                }
            }
            return sInstance
        }
    }
}