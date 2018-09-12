package com.musicplayer.aow.delegates.softcode

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.RecyclerView
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.softcode.adapters.*

class SoftCodeAdapter {

    fun generatString(len: Int): String {
        return Generator().randomString(len)
    }

    fun getJsonString(context: Context, url: String): String? {
        return HttpHelperFunctions(context.applicationContext)
                .getJsonString(url)
    }

    fun sendNotification(context: Context, title: String?, msg: String?){
        NotificationFunctions(context).sendNotification(title, msg)
    }
    
    fun downloadSuccessNotification(context: Context,title: String?, location:String?, msg: String?){
        NotificationFunctions(context).downloadSuccessNotification(title, location, msg)
    }

    fun downloadFileAsync(context: Context, downloadUrl: String, track: Track) {
        DownloadFileAsync(context).downloadFileAsync(downloadUrl, track)
    }

    fun convertToBitmap(drawable: Drawable, widthPixels: Int, heightPixels: Int): Bitmap? {
        val mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        drawable.setBounds(0, 0, widthPixels, heightPixels)
        drawable.draw(canvas)

        return mutableBitmap
    }

    fun deleteSongFromPhone(context: Context, model: Track){
        SongFunctions().deletSongFromPhone(context, model)
    }

    fun openAlbumActivity(context: Context, album: Album){
        AlbumFunctions().openAlbumActivity(context, album)
    }

    fun getAlbum(context: Context, album_id: String): Album {
        return AlbumFunctions().getAlbum(context, album_id)
    }

    fun deleteAlbum(context: Context, album: String, id: Boolean = true){
        AlbumFunctions().deleteAlbum(context, album, id)
    }

    fun deleteArtist(context: Context, artist: String, id: Boolean = true){
        ArtistFunctions().deleteArtist(context, artist, id)
    }

    fun getAlbumTracks(context: Context, album: String, id: Boolean = true): ArrayList<Track> {
        return AlbumFunctions().getAlbumTracks(context, album, id)
    }

    fun getArtistTracks(context: Context, artist: String, id: Boolean = true): ArrayList<Track> {
        return ArtistFunctions().getArtistTracks(context, artist, id)
    }

    fun createPlaylist(context: Context, name: String): Long {
        return PlaylistFunctions().createPlaylist(context, name)
    }

    fun getFavoritesId(context: Context): Long {
        return PlaylistFunctions().getFavoritesId(context)
    }

    fun getSongCountForPlaylist(context: Context, playlistId: Long): Int {
        return PlaylistFunctions().getSongCountForPlaylist(context, playlistId)
    }

    fun getPlaylistTracks(context: Context, id: Long): PlayList {
        return PlaylistFunctions().getPlaylistTracks(context, id)
    }

    fun getPlaylistTracksIds(context: Context, id: Long): ArrayList<String> {
        return PlaylistFunctions().getPlaylistTracksIds(context, id)
    }

    fun addSongToPlayList(context: Context, playListId: Long, songId: Int){
        PlaylistFunctions().addSongToPlayList(context, playListId, songId)
    }

    fun addSongToPlaylist(
            context: Context,
            mylist: RecyclerView,
            mSelectPlaylistDialog: BottomSheetDialog,
            track: Track = Track(),
            tracks: ArrayList<Track>? = ArrayList(),
            arrayOfSongs: Boolean = false){
        PlaylistFunctions().addSongToPlaylist(context, mylist, mSelectPlaylistDialog, track, tracks, arrayOfSongs)
    }

    fun removeSongsFromPlayList(context: Context, id: Long, path: String){
        PlaylistFunctions().removeSongs(context, id, path)
    }


    /**
     * Offline save favorite playlist
     * and offline saved favorite tracks
     */
}