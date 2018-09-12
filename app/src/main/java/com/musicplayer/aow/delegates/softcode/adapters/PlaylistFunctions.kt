package com.musicplayer.aow.delegates.softcode.adapters

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Audio.PlaylistsColumns
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.ui.main.library.songs.view.dialog.adapter.PlaylistDialogAdapter
import com.musicplayer.aow.utils.CursorDB
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import java.io.File





class PlaylistFunctions{

    private var playlisDatabase: PlaylistDatabase? = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    fun createPlaylist(context: Context, name: String): Long {
        if (name.isNotEmpty()) {
            val resolver = context.contentResolver
            val cols = arrayOf(
                    MediaStore.Audio.PlaylistsColumns.NAME
            )
            val whereclause = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'"
            val cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cols, whereclause,
                    null, null)
            if (cur.count <= 0) {
                val values = ContentValues(1)
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name)
                try {
                    val uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values)
                    cur.close()
                    return uri.lastPathSegment.toLong()
                }catch (e:Exception){

                }
            }
            cur.close()
            return -1
        }
        return -1
    }

    //Add to Playlist Operation
    fun addSongToPlaylist(
            context: Context,
            mylist: RecyclerView,
            mSelectPlaylistDialog: BottomSheetDialog,
            model: Track,
            tracks: ArrayList<Track>? = ArrayList(),
            arrayOfSongs: Boolean = false){
        AppExecutors.instance?.diskIO()?.execute{
            var list: ArrayList<PlayList> = ArrayList()
            list = playlisDatabase?.playlistDAO()?.fetchAllPlayListWithNoRecentlyPlayedNLD() as ArrayList<PlayList>
            list.sortedWith(compareBy({ (it.name)!!.toLowerCase() }))
            val adapter = PlaylistDialogAdapter(list, model, tracks, mSelectPlaylistDialog, arrayOfSongs)
            val layoutManager = PreCachingLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(context))
            mylist.setHasFixedSize(true)
            mylist.layoutManager = layoutManager
            mylist.adapter = adapter
        }
    }

    fun addSongToPlayList(context: Context, playListId: Long, songId: Int) {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId)
        val cursor = context.contentResolver.query(uri, arrayOf("count(*)"), null, null, null)
        cursor.moveToFirst()
        var last = cursor.getInt(0)
        cursor.close()
        val value = ContentValues()
        value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, ++last)
        value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId)
        context.contentResolver.insert(uri, value)
    }

    fun removeSongs(context: Context, id: Long, path: String){
        val file = File(path)
        if (file.exists()){
            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
            mFilePath += path
            val rootUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id)
            context.contentResolver.delete( rootUri,
                    MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", arrayOf( path ) )
        }else{
            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
            mFilePath += path
            val rootUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id)
            context.contentResolver.delete( rootUri,
                    MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", arrayOf(path) )
        }
    }

    fun getPlaylistTracks(context: Context, id: Long): PlayList {
        val playlist = PlayList()
        val playListUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id)
        val cursor = context.contentResolver.query(playListUri, null, null, null, null)
        if(cursor != null){
            while (cursor.moveToNext()){
                playlist.addSong(CursorDB().cursorToMusic(cursor))
            }
        }
        cursor.close()
        return playlist
    }

    fun getPlaylistTracksIds(context: Context, id: Long): ArrayList<String> {
        val songs = ArrayList<String>()
        val playListUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id)
        val cursor = context.contentResolver.query(playListUri, null, null, null, null)
        if(cursor != null){
            while (cursor.moveToNext()){
                songs.add(CursorDB().cursorToMusicId(cursor))
            }
        }
        cursor.close()
        return songs
    }

    fun getSongCountForPlaylist(context: Context, playlistId: Long): Int {
        var c = context.contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                arrayOf(BaseColumns._ID), null, null, null)

        if (c != null) {
            var count = 0
            if (c.moveToFirst()) {
                count = c.count
            }
            c.close()
            c = null
            return count
        }

        return 0
    }

    fun getFavoritesId(context: Context): Long {
        var favorites_id: Long = -1
        val favorites_where = PlaylistsColumns.NAME + "='" + "Favorites" + "'"
        val favorites_cols = arrayOf(BaseColumns._ID)
        val favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(favorites_uri, favorites_cols, favorites_where, null, null)
        if (cursor.count <= 0) {
            favorites_id = createPlaylist(context, "Favorites")
        } else {
            cursor.moveToFirst()
            favorites_id = cursor.getLong(0)
            cursor.close()
        }
        return favorites_id
    }

    fun getPlaylistName(mContext: Context, playlist_id: Long): String {
        val where = BaseColumns._ID + "=" + playlist_id
        val cols = arrayOf(MediaStore.Audio.PlaylistsColumns.NAME)
        val uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val cursor = mContext.contentResolver.query(uri, cols, where, null, null)
        if (cursor == null){
            return ""
        }
        if (cursor.count <= 0)
            return ""
        cursor.moveToFirst()
        val name = cursor.getString(0)
        cursor.close()
        return name
    }
}