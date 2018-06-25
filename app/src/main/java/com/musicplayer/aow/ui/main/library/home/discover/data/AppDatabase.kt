package com.musicplayer.aow.ui.main.library.home.discover.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(PlaylistObject::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playListObjectDAO(): PlayListObjectDAO

    companion object {

        private val LOG_TAG = AppDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "discovery"
        private var sInstance: AppDatabase? = null

        fun getsInstance(context: Context): AppDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                            AppDatabase.DATABASE_NAME).allowMainThreadQueries().build()
                }
            }
            return sInstance
        }
    }
}
