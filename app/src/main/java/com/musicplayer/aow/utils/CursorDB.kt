package com.musicplayer.aow.utils

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.util.ArrayMap
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import java.io.File


/**
 * Created by Arca on 11/9/2017.
 */
class CursorDB {

    private val TAG = this.javaClass.name

    var albumArt: Cursor? = null
    private var mContext:Context? = null

    private val MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private var WHERE = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
            + MediaStore.Audio.Media.SIZE + ">0" )
    private val ORDER_BY = MediaStore.Audio.Media.TITLE + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Media.DATA, // the real path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE)

    fun songs(context: Context): Cursor? {
        return context.contentResolver.query(
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY)
    }

    fun callCursor(context: Context): Cursor? {
        mContext = context
        albumArt = Injection.provideContext()!!.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                null,null, null)
        return context.contentResolver.query(
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY)
    }

    fun albumaCursor(context: Context): Cursor? {
        return context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                null,null, null)
    }

    fun cursorToMusic(cursor: Cursor, mMap: ArrayMap<Any,String>, indexPosition: Int): Song {
        val realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
        val song: Song?
        song = Song()
        song.id = indexPosition
        song.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
        var displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        if (displayName.endsWith(".mp3")) {
            displayName = displayName.substring(0, displayName.length - 4)
        }
        song.displayName = displayName
        if (song.displayName == null){
            song.displayName = "Unknown"
        }
        song.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        if (song.artist == null){
            song.artist = "Unknown"
        }
        song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        if (song.album == null){
            song.album = "Unknown"
        }
        if (mMap.containsKey(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)))){
            song.albumArt = mMap[cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))]
        }else{
            song.albumArt = ""
        }
        song.path = realPath
        song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        song.size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

        return song
    }


    fun cursorToMusic(cursor: Cursor): Song {
        val realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
        val song: Song?
        song = Song()
        song.id  = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toInt()
        song.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
        var displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        if (displayName.endsWith(".mp3")) {
            displayName = displayName.substring(0, displayName.length - 4)
        }
        song.displayName = displayName
        if (song.displayName == null){
            song.displayName = "Unknown"
        }
        song.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        if (song.artist == null){
            song.artist = "Unknown"
        }
        song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        if (song.album == null){
            song.album = "Unknown"
        }
        
        song.albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))

        song.path = realPath
        song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        song.size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

        return song
    }

    fun cursorToMusicId(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID))
    }

    fun cursorToMusicPlaylist(cursor: Cursor): Song {
        val realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
        val song: Song?
        song = Song()
        song.id  = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toInt()
        song._id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)).toLong()
        song.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
        var displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        if (displayName.endsWith(".mp3")) {
            displayName = displayName.substring(0, displayName.length - 4)
        }
        song.displayName = displayName
        if (song.displayName == null){
            song.displayName = "Unknown"
        }
        song.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
        if (song.artist == null){
            song.artist = "Unknown"
        }
        song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        if (song.album == null){
            song.album = "Unknown"
        }

        song.albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))

        song.path = realPath
        song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        song.size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

        return song
    }

    fun cursorToAlbumList(cursor: Cursor): Album {
        val album = Album()
        album.id  = cursor.position
        album.album_id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
        album.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
        album.albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        if (album.albumName == null){
            album.albumName = "Unknown"
        }
        album.numberOfSongs = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
        album.numberOfSongsForArtists = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
        album.albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
        album.albumKey = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_KEY))
        album.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
        if (album.artist == null){
            album.artist = "Unknown"
        }
        return album
    }

    fun cursorToArtistsList(cursor: Cursor): Artists {
        val artists = Artists()
        artists.id  = cursor.position
        artists.artist_id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
        artists.artist_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
        artists.numberOfSongs = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
        artists.numberOfAlbums = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
        return artists
    }

    fun cursorToPlayList(cursor: Cursor): PlayList{
        val playList = PlayList()
        playList.name =  cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME))
        playList._id =  cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID))
        return playList
    }

    fun getAlbum(context: Context, album_id: String): Album{
        var album = Album()
        val alb = context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(
                        MediaStore.Audio.Albums.ALBUM, // the real path
                        MediaStore.Audio.Albums.ARTIST,
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM_ART,
                        MediaStore.Audio.Albums.ALBUM_KEY,
                        MediaStore.Audio.Albums.NUMBER_OF_SONGS),
                MediaStore.Audio.Albums._ID + "=?",
                arrayOf(album_id),
                null)
        if (alb != null) {
            while (alb.moveToNext()) {
                album = cursorToAlbumList(alb)
            }
            alb.close()
        }
        return album
    }


    fun getAlbumSongs(context: Context, album: String, id: Boolean = true): Cursor{
        WHERE = if(id) {
            (MediaStore.Audio.Media.ALBUM_ID + "=\"$album\" AND " + MediaStore.Audio.Media.SIZE + ">0")
        }else{
            (MediaStore.Audio.Media.ALBUM + "=\"$album\" AND " + MediaStore.Audio.Media.SIZE + ">0")
        }
        return context.contentResolver.query(
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY)
    }

    fun getArtistSongs(context: Context, artist: String, id: Boolean = true): Cursor{
        WHERE = if (id){
            (MediaStore.Audio.Media.ARTIST_ID + "=\"$artist\" AND " + MediaStore.Audio.Media.SIZE + ">0")
        }else {
            (MediaStore.Audio.Media.ARTIST + "=\"$artist\" AND " + MediaStore.Audio.Media.SIZE + ">0")
        }
        return context.contentResolver.query(
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY)
    }

    fun deleteMusic(cursor: Cursor, context: Context) {
        val realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
        val file = File(realPath)
        if (file.exists()){
            if (file.delete()) {
                var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                mFilePath += realPath
                val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                context.contentResolver.delete( rootUri,
                        MediaStore.Audio.Media.DATA + "=?", arrayOf( realPath ) )
            }
        }else{
            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
            mFilePath += realPath
            val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
            context.contentResolver.delete( rootUri,
                    MediaStore.Audio.Media.DATA + "=?", arrayOf( realPath ) )
        }
    }

    fun deleteMusic(context: Context, name: String?){
        val file = File(name)
        if (file.exists()){
            if (file.delete()) {
                var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                mFilePath += name
                val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                context.contentResolver.delete( rootUri,
                        MediaStore.Audio.Media.DATA + "=?", arrayOf( name ) )
            }
        }else{
            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
            mFilePath += name
            val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
            context.contentResolver.delete( rootUri,
                    MediaStore.Audio.Media.DATA + "=?", arrayOf( name ) )
        }
    }


    companion object {
        public var sInstance: CursorDB? = null
        val instance: CursorDB?
            get() {
                if (sInstance == null) {
                    synchronized(Settings.Companion) {
                        if (sInstance == null) {
                            sInstance = CursorDB()
                        }
                    }
                }
                return sInstance
            }
    }

}