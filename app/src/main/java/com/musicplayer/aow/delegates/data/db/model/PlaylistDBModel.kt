package com.musicplayer.aow.delegates.data.db.model

import android.content.Context
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter

class PlaylistDBModel(var context: Context) {
    private var playlisDatabase: PlaylistDatabase? = PlaylistDatabase.getsInstance(context)

    fun initPlaylist(){
        AppExecutors.instance?.diskIO()?.execute {
            val favoritetracks = playlisDatabase?.playlistDAO()?.fetchPlayListName("Favorites")
            if (favoritetracks == null){
                val newPlayList = PlayList()
                newPlayList.mxp_id = SoftCodeAdapter().generatString(32)
                newPlayList.name = "Favorites"
                playlisDatabase?.playlistDAO()?.insertOnePlayList(newPlayList)
            }
        }
    }
}