package com.musicplayer.aow.delegates.data.db.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.db.dao.PlaylistDAO
import com.musicplayer.aow.delegates.data.model.PlayList

@Database(entities = arrayOf(PlayList::class), version = 1, exportSchema = false)
abstract class PlaylistDatabase: RoomDatabase(){
    abstract fun playlistDAO() : PlaylistDAO

    companion object {
        private val LOG_TAG = PlaylistDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "playlist"
        private var sInstance: PlaylistDatabase? = null

        fun getsInstance(context: Context): PlaylistDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, PlaylistDatabase::class.java,
                            PlaylistDatabase.DATABASE_NAME).build()
                }
            }
            return sInstance
        }
    }
}