package com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.musicplayer.aow.delegates.data.model.PlayList

@Database(entities = arrayOf(PlayList::class), version = 1, exportSchema = false)
abstract class PlayListFavDatabase: RoomDatabase(){
    abstract fun playlistFavDAO() : PlaylistFavDAO

    companion object {
        private val LOG_TAG = PlayListFavDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "playlist"
        private var sInstance: PlayListFavDatabase? = null

        fun getsInstance(context: Context): PlayListFavDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context.applicationContext, PlayListFavDatabase::class.java,
                            PlayListFavDatabase.DATABASE_NAME).allowMainThreadQueries().build()
                }
            }
            return sInstance
        }
    }
}