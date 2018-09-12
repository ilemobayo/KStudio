package com.musicplayer.aow.delegates.data.db.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.db.dao.DiscoveryDAO
import com.musicplayer.aow.delegates.data.db.model.DiscoveryModel

@Database(entities = arrayOf(DiscoveryModel::class), version = 1, exportSchema = false)
abstract class DiscoveryDatabase: RoomDatabase() {
    abstract fun discoveryDAO(): DiscoveryDAO

    companion object {

        private val LOG_TAG = DiscoveryDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "foryou"
        private var sInstance: DiscoveryDatabase? = null

        fun getsInstance(context: Context): DiscoveryDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, DiscoveryDatabase::class.java,
                            DiscoveryDatabase.DATABASE_NAME).build()
                }
            }
            return sInstance
        }
    }
}