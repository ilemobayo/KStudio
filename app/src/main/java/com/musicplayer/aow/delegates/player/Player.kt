package com.musicplayer.aow.delegates.player

import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.webkit.URLUtil
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.ui.nowplaying.NowPlaying
import org.jetbrains.anko.doAsync
import java.io.IOException
import java.util.*




/**
 * Created with Android Studio.
 * User:
 * Date:
 * Time:
 * Desc: Player
 */
class Player private constructor() : IPlayback,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener {

    override var mPlayer: MediaPlayer? = null

    private val mPlayOnFocusGain: Boolean = false

    var mPlayList: PlayList? = PlayList()
    // Default size 2: for service and UI
    private val mCallbacks = ArrayList<IPlayback.Callback>(2)

    // Player status
    private var isPaused: Boolean = false

    override val isPlaying: Boolean
        get() = if (mPlayer != null){mPlayer!!.isPlaying}else{ false }

    override val progress: Int
        get() = mPlayer!!.currentPosition

    override val playingSong: Song?
        get() = if (mPlayList!!.currentSong == null){ null }else{mPlayList!!.currentSong}

    override var playingList: PlayList? = null
        get() = mPlayList

    init {
        mPlayer = MediaPlayer()
        Equalizer(0, mPlayer!!.audioSessionId)
        mPlayList = PlayList()
        mPlayer!!.setOnCompletionListener(this)
        mPlayer!!.setAuxEffectSendLevel(1.0f)
        //mediaPlayer!!.setOnBufferingUpdateListener(this)
    }

    override fun setPlayList(list: PlayList) {
        mPlayList = list
    }

    override fun play(): Boolean {
        if (isPaused) {
            if (mPlayer != null) {
                isPaused = false
                mPlayer!!.start()
                notifyPlayStatusChanged(true)
                return true
            }
        }
        if (mPlayList!!.prepare()) {
            val song = mPlayList!!.currentSong
            try {
                isPaused = true
                notifyPlayStatusChanged(false)
                mPlayer!!.reset()
                val stream: Boolean = URLUtil.isHttpUrl(song?.path) || URLUtil.isHttpsUrl(song?.path)
                if(stream){
                    notifyTriggerLoading(true)  //trigger loading
                    mPlayer!!.setDataSource(song?.path)
                    mPlayer!!.prepareAsync()
                    mPlayer!!.setOnPreparedListener({ mp ->
                        mp.start()
                        isPaused = false
                        notifyPlayStatusChanged(true)
                    })
                }else {
                    val url = song?.path
                    mPlayer!!.setDataSource(url)
                    mPlayer!!.prepare()
                    mPlayer!!.start()
                    isPaused = false
                    notifyPlayStatusChanged(true)
                }
                return true
            } catch (e: IOException) {
                isPaused = true
                mPlayer!!.pause()
                notifyPlayStatusChanged(false)
                playNext()
                return false
            }
        }else{
            isPaused = true
            notifyPlayStatusChanged(false)
        }
        return false
    }


    override fun play(list: PlayList): Boolean {
        isPaused = false
        setPlayList(list)
        return play()
    }

    override fun play(list: PlayList, startIndex: Int): Boolean {
        if (startIndex < 0 || startIndex >= list.numOfSongs) return false

        isPaused = false
        list.playingIndex = startIndex
        setPlayList(list)
        doAsync {
            NowPlaying.instance!!.setSongs(list.songs)
        }
        return play()
    }


    override fun play(song: Song): Boolean {
        isPaused = false
        mPlayList!!.songs?.clear()
        mPlayList!!.songs?.add(song)
        return play()
    }

    override fun playLast(): Boolean {
        isPaused = false
        val hasLast = mPlayList!!.hasLast()
        if (hasLast) {
            val last = mPlayList!!.last()
            play()
            notifyPlayLast(last)
            return true
        }
        return false
    }

    override fun playNext(): Boolean {
        isPaused = false
        val hasNext = mPlayList!!.hasNext(true)
        if (hasNext) {
            val next = mPlayList!!.nextWithIndex()
            //play()
            play(mPlayList!!, next.playingIndex!!)
            notifyPlayNext(next.song!!)
            return true
        }
        return false
    }

    override fun pause(): Boolean {
        if (mPlayer == null) return false
        
        if (mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            isPaused = true
            notifyPlayStatusChanged(false)
            return true
        }
        return false
    }

    override fun seekTo(progress: Int): Boolean {
        if (mPlayList!!.songs == null) return false

        val currentSong = mPlayList!!.currentSong
        if (currentSong != null) {
            if (currentSong.duration <= progress) {
                onCompletion(mPlayer)
            } else {
                mPlayer!!.seekTo(progress)
            }
            return true
        }
        return false
    }


    override fun setPlayMode(playMode: PlayMode) {
        mPlayList!!.playMode = playMode
    }

    // Listeners
    override fun onCompletion(mp: MediaPlayer?) {
        var next: Song? = null
        // There is only one limited play mode which is list, player should be stopped when hitting the list end
        if (mPlayList!!.playMode === PlayMode.LIST && mPlayList!!.playingIndex == (mPlayList!!.numOfSongs - 1)) {
            // In the end of the list
            // Do nothing, just deliver the callback
        } else if (mPlayList!!.playMode === PlayMode.SINGLE) {
            next = mPlayList!!.currentSong
            play()
        } else {
            val hasNext = mPlayList!!.hasNext(true)
            if (hasNext) {
                next = mPlayList!!.next()
                play()
            }
        }
        notifyComplete(next)
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        //
    }

    override fun releasePlayer() {
        mPlayList = null
        mPlayer!!.reset()
        mPlayer!!.release()
        mPlayer = null
        sInstance = null
    }

    //add to playlist
    fun insertnext(index: Int, value: Song) {
        //var queue = Queue(mPlayList!!.songs)
        val playIndex = index + 1
        val body = mPlayList!!.songs as ArrayList
        val queue = ArrayList<Song>()
        if (body.size >= playIndex) {
            if (body.size == playIndex - 1) {
                queue.addAll(body)
                queue.add(value)
            }else{
                queue.addAll(body.subList(0, playIndex))
                queue.add(value)
                queue.addAll(body.subList(playIndex, body.size))
            }
        }else{
            queue.addAll(body)
            queue.add(value)
        }
        mPlayList!!.songs = queue
        NowPlaying.instance!!.setUpdate()
    }

    fun insertnext(index: Int, value: ArrayList<Song>) {
        val playIndex = index + 1
        val body = mPlayList!!.songs as ArrayList
        val queue = ArrayList<Song>()
        if (body.size >= playIndex) {
            if (body.size == playIndex - 1) {
                queue.addAll(body)
                queue.addAll(value)
            }else{
                queue.addAll(body.subList(0, playIndex))
                queue.addAll(value)
                queue.addAll(body.subList(playIndex, body.size))
            }
        }else{
            queue.addAll(body)
            queue.addAll(value)
        }
        mPlayList!!.songs = queue
        NowPlaying.instance!!.setUpdate()
    }

    fun updatePlaylist(value: ArrayList<Song>){
        mPlayList!!.songs = value
    }

    // Callbacks

    override fun registerCallback(callback: IPlayback.Callback) {
        mCallbacks.add(callback)
    }

    override fun unregisterCallback(callback: IPlayback.Callback) {
        mCallbacks.remove(callback)
    }

    override fun removeCallbacks() {
        mCallbacks.clear()
    }

    private fun notifyPlayStatusChanged(isPlaying: Boolean) {
        for (callback in mCallbacks) {
            callback.onPlayStatusChanged(isPlaying)
        }
    }

    private fun notifyTriggerLoading(isLoading: Boolean){
       for (callback in mCallbacks){
           callback.onTriggerLoading(isLoading)
       }
    }

    private fun notifyPlayLast(song: Song) {
        for (callback in mCallbacks) {
            callback.onSwitchLast(song)
        }
    }

    private fun notifyPlayNext(song: Song) {
        for (callback in mCallbacks) {
            callback.onSwitchNext(song)
        }
    }

    private fun notifyComplete(song: Song?) {
        for (callback in mCallbacks) {
            callback.onComplete(song)
        }
    }

    companion object {

        private const val TAG = "Player"

        @Volatile private var sInstance: Player? = null

        val instance: Player?
            get() {
                if (sInstance == null) {
                    synchronized(Player.Companion) {
                        if (sInstance == null) {
                            sInstance = Player()
                        }
                    }
                }
                return sInstance
            }
    }
}
