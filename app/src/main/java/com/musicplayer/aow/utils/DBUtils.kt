package com.musicplayer.aow.utils

import android.content.Context
import android.os.Environment
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.Folder
import com.musicplayer.aow.delegates.data.model.PlayList
import java.io.File
import java.util.*


object DBUtils {

    fun generateFavoritePlayList(context: Context?): PlayList {
        val favorite = PlayList()
        favorite.isFavorite = true
        favorite.name = context?.getString(R.string.mp_play_list_favorite)
        return favorite
    }

    fun generateSongsPlayList(context: Context?): PlayList {
        val songs = PlayList()
        songs.name = context?.getString(R.string.mp_play_list_songs)
        return songs
    }

    fun generateSongsNowPlaying(context: Context?): PlayList {
        val songs = PlayList()
        songs.name = context?.getString(R.string.mp_play_list_nowplaying)
        return songs
    }

    //
    fun generateDefaultFolders(): List<Folder> {
        val defaultFolders = ArrayList<Folder>(4)
        val internalDir = Environment.getExternalStorageDirectory()
        val sdcardDir = Environment.getExternalStorageDirectory().parentFile.parentFile
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val all = File("/storage/")
        defaultFolders.add(FileUtilities.folderFromDir(internalDir))
        defaultFolders.add(FileUtilities.folderFromDir(sdcardDir))
        defaultFolders.add(FileUtilities.folderFromDir(downloadDir))
        defaultFolders.add(FileUtilities.folderFromDir(musicDir))
        defaultFolders.add(FileUtilities.folderFromDir(all))
        return defaultFolders
    }
}
