package com.musicplayer.aow.delegates.data.source

import com.musicplayer.aow.delegates.data.model.Folder
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import rx.Observable

/**
 * Created with Android Studio.
 * User:
 * Date:
 * Time:
 * Desc: AppContract
 */
internal interface AppContract {

    // Play List
    fun createDefaultAllSongs()

    fun playLists(): Observable<List<PlayList>>?

    fun cachedPlayLists(): MutableList<PlayList>?

    fun create(playList: PlayList): Observable<PlayList>

    fun update(playList: PlayList): Observable<PlayList>

    fun delete(playList: PlayList): Observable<PlayList>

    // Folder

    fun folders(): Observable<MutableList<Folder>>

    fun create(folder: Folder): Observable<Folder>

    fun create(folders: List<Folder>): Observable<MutableList<Folder>>

    fun update(folder: Folder): Observable<Folder>

    fun delete(folder: Folder): Observable<Folder>

    // Song

    fun insert(songs: MutableList<Song>): Observable<MutableList<Song>>

    fun update(song: Song): Observable<Song>

    fun setSongAsFavorite(song: Song, favorite: Boolean): Observable<Song>

    fun setInitAllSongs(playlist: PlayList):Observable<PlayList>

    fun playlist(name: String): Observable<PlayList>

}
