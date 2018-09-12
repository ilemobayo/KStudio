package com.musicplayer.aow.delegates.data.db.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.db.dao.TrackDAO
import com.musicplayer.aow.delegates.data.model.Track

@Database(entities = arrayOf(Track::class), version = 1, exportSchema = false)
abstract class TrackDatabase: RoomDatabase() {
    abstract fun trackDAO(): TrackDAO

    companion object {

        private val LOG_TAG = TrackDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "track"
        private var sInstance: TrackDatabase? = null

        fun getsInstance(context: Context): TrackDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, TrackDatabase::class.java,
                            TrackDatabase.DATABASE_NAME).build()
                }
            }
            return sInstance
        }
    }
}