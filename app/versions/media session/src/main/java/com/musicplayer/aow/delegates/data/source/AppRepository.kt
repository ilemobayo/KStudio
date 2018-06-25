package com.musicplayer.aow.delegates.data.source

import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Folder
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.db.LiteOrmHelper
import rx.Observable
import java.util.*

class AppRepository () : AppContract {

    private val mLocalDataSource: AppLocalDataSource = AppLocalDataSource(Injection.provideContext(), LiteOrmHelper.instance)

    private var mCachedPlayLists: List<PlayList>? = null

    // Play List
    override fun playLists(): Observable<List<PlayList>> {
        return mLocalDataSource.playLists()
                .doOnNext { playLists -> mCachedPlayLists = playLists }
    }


    override fun cachedPlayLists(): MutableList<PlayList> {
        return if (mCachedPlayLists == null) {
            ArrayList(0)
        } else mCachedPlayLists as MutableList<PlayList>
    }

    override fun create(playList: PlayList): Observable<PlayList> {
        return mLocalDataSource.create(playList)
    }

    override fun update(playList: PlayList): Observable<PlayList> {
        return mLocalDataSource.update(playList)
    }

    override fun delete(playList: PlayList): Observable<PlayList> {
        return mLocalDataSource.delete(playList)
    }

    // Folders

    override fun folders(): Observable<MutableList<Folder>> {
        return mLocalDataSource.folders()
    }

    override fun create(folder: Folder): Observable<Folder> {
        return mLocalDataSource.create(folder)
    }

    override fun create(folders: List<Folder>): Observable<MutableList<Folder>> {
        return mLocalDataSource.create(folders)
    }

    override fun update(folder: Folder): Observable<Folder> {
        return mLocalDataSource.update(folder)
    }

    override fun delete(folder: Folder): Observable<Folder> {
        return mLocalDataSource.delete(folder)
    }

    override fun insert(songs: MutableList<Song>): Observable<MutableList<Song>> {
        return mLocalDataSource.insert(songs)
    }

    override fun update(song: Song): Observable<Song> {
        return mLocalDataSource.update(song)
    }

    override fun setSongAsFavorite(song: Song, favorite: Boolean): Observable<Song> {
        return mLocalDataSource.setSongAsFavorite(song, favorite)
    }

    override fun createDefaultAllSongs(){
        mLocalDataSource.createDefaultAllSongs()
    }

    override fun setInitAllSongs(playlist: PlayList): Observable<PlayList> {
        return mLocalDataSource.setInitAllSongs(playlist)
    }

    override fun playlist(name: String): Observable<PlayList>{
        return mLocalDataSource.playlist(name)
    }

    companion object {

        @Volatile private var sInstance: AppRepository? = null

        val instance: AppRepository?
            get() {
                if (sInstance == null) {
                    synchronized(AppRepository::class.java) {
                        if (sInstance == null) {
                            sInstance = AppRepository()
                        }
                    }
                }
                return sInstance
            }
    }
}

