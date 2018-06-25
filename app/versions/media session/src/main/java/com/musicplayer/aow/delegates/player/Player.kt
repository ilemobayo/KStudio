package com.musicplayer.aow.delegates.player

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.util.Log
import android.webkit.URLUtil
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.ChangePlaystate
import com.musicplayer.aow.ui.nowplaying.NowPlaying
import com.musicplayer.aow.utils.ApplicationSettings
import com.musicplayer.aow.utils.StorageUtil
import com.rxandroidnetworking.RxAndroidNetworking
import org.jetbrains.anko.doAsync
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator


/**
 * Created with Android Studio.
 * User:
 * Date:
 * Time:
 * Desc: Player
 */
class Player private constructor() : IPlayback,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    var mPlayer: MediaPlayer? = null

    var mPlayList: PlayList? = null
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
        get() = if (mPlayList!! == null){ null }else{mPlayList}

    init {
        mPlayer = MediaPlayer()
        Equalizer(0, mPlayer!!.audioSessionId)
        mPlayList = PlayList()
        mPlayer!!.setOnCompletionListener(this)
        //mediaPlayer!!.setOnBufferingUpdateListener(this)
    }

    override fun setPlayList(list: PlayList) {
        var list = list
        if (list == null) {
            list = PlayList()
        }
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
                notifyPlayStatusChanged(true)
                mPlayer!!.reset()
                var stream: Boolean = URLUtil.isHttpUrl(song.path) || URLUtil.isHttpsUrl(song.path)
                if(stream){
                    var data_file = File(Injection.provideContext()!!.externalCacheDir, "audio/${song.displayName}.mp3")
                    if (data_file.exists()){
                        Log.e("Streaming", "loading from local")
                        mPlayer!!.setDataSource(data_file.absolutePath)
                        mPlayer!!.prepare()
                        mPlayer!!.start()
                    }else {
                        Log.e("Streaming", "loading from network.")
                        mPlayer!!.setDataSource(song.path)
                        mPlayer!!.prepareAsync()
                        mPlayer!!.setOnPreparedListener({ mp ->
                            mp.start()
                        })
                        val action = ApplicationSettings().cacheaction
                        var save: StorageUtil? = StorageUtil(Injection!!.provideContext()!!.applicationContext)
                        var shakeSettingsState = save!!.loadStringValue(action)
                        if (shakeSettingsState!!.equals("on")) {
                            val file = File(Injection.provideContext()!!.externalCacheDir, "audio/")
                            //download file
                            RxAndroidNetworking.download("${song.path}", "${file.absolutePath}", "${song.displayName}.mp3")
                                    .build()
                                    .setDownloadProgressListener({ bytesDownloaded, totalBytes ->

                                    })
                                    .downloadObservable
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : rx.Observer<String> {
                                        override fun onError(e: Throwable?) {
                                            Log.e("Streaming", "download error.")
                                        }

                                        override fun onNext(t: String?) {
                                        }

                                        override fun onCompleted() {
                                            try {
                                                val keygenerator = KeyGenerator.getInstance("DES")
                                                val myDesKey = keygenerator.generateKey()

                                                val desCipher: Cipher
                                                desCipher = Cipher.getInstance("DES")

                                                var data_file = File(Injection.provideContext()!!.externalCacheDir, "audio/${song.displayName}.mp3")
                                                if (data_file.exists()){
                                                    val file = File(Injection.provideContext()!!.externalCacheDir, "/audio/"  + "${song.displayName}_encode.mp3")
                                                    desCipher.init(Cipher.ENCRYPT_MODE, myDesKey)
                                                    var byteArray = desCipher.doFinal(data_file.readBytes())

                                                    if (!file.exists()) {
                                                        file.createNewFile()
                                                    }
                                                    val fos = FileOutputStream(file)
                                                    fos.write(byteArray)
                                                    fos.close()
                                                    Log.e("Streaming", "encrypted file.")

                                                    val dfile = File(Injection.provideContext()!!.externalCacheDir, "/audio/"  + "${song.displayName}_decode.mxp")
                                                    val old_file = File(Injection.provideContext()!!.externalCacheDir, "/audio/"  + "${song.displayName}_encode.mp3")
                                                    desCipher.init(Cipher.DECRYPT_MODE, myDesKey)
                                                    byteArray = desCipher.doFinal(old_file.readBytes())
                                                    if (!dfile.exists()) {
                                                        dfile.createNewFile()
                                                    }
                                                    val dfos = FileOutputStream(dfile)
                                                    dfos.write(byteArray)
                                                    dfos.close()
                                                    Log.e("Streaming", "Decrypted file.")
                                                }

                                            } catch (e: Exception) {
                                                println("Exception")
                                            }

                                            Log.e("Streaming", "File Downloaded.")
                                        }
                                    })
                        }
                    }
                }else {
                    var url = song.path
                    mPlayer!!.setDataSource(url)
                    mPlayer!!.prepare()
                    mPlayer!!.start()
                }
                //broadcast the change of currently on recyclerview
                RxBus.instance!!.post(ChangePlaystate(true))
                return true
            } catch (e: IOException) {
                notifyPlayStatusChanged(false)
                playNext()
                return false
            }
        }else{
            notifyPlayStatusChanged(false)
        }
        return false
    }


    override fun play(list: PlayList): Boolean {
        if (list == null) return false

        isPaused = false
        setPlayList(list)
        return play()
    }

    override fun play(list: PlayList, startIndex: Int): Boolean {

        if (list == null || startIndex < 0 || startIndex >= list.numOfSongs) return false

        isPaused = false
        list.playingIndex = startIndex
        setPlayList(list)
        doAsync {
            NowPlaying.instance!!.setSongs(list.songs)
        }
        return play()
    }


    override fun play(song: Song): Boolean {
        if (song == null) return false

        isPaused = false
        mPlayList!!.songs.clear()
        mPlayList!!.songs.add(song)
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
        if (mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            isPaused = true
            notifyPlayStatusChanged(false)
            return true
        }
        return false
    }

    override fun seekTo(progress: Int): Boolean {
        if (mPlayList!!.songs.isEmpty()) return false

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

    fun getDuration(): Int {
        return mPlayList!!.currentSong.duration
    }

    override fun setPlayMode(playMode: PlayMode) {
        mPlayList!!.playMode = playMode
    }

    // Listeners
    override fun onCompletion(mp: MediaPlayer?) {
        var next: Song? = null
        // There is only one limited play mode which is list, player should be stopped when hitting the list end
        if (mPlayList!!.playMode === PlayMode.LIST && mPlayList!!.playingIndex === mPlayList!!.numOfSongs - 1) {
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

    override fun onBufferingUpdate(mp: MediaPlayer?, percentage: Int) {
        //
    }

    override fun releasePlayer() {
        mPlayList = null
        mPlayer!!.reset()
        mPlayer!!.release()
        mPlayer = null
        sInstance = null
    }

    fun playStream(playList: PlayList, index: Int){
        mPlayList = playList
        playList.playingIndex = index
        playStream()
    }

    private fun playStream(){
        val song = mPlayList!!.currentSong
        try {
            notifyPlayStatusChanged(true)
            mPlayer!!.reset()
            mPlayer!!.setDataSource(song.path)
            mPlayer!!.prepareAsync()
            mPlayer!!.setOnPreparedListener({ mp ->
                mp.start()
            })
        } catch (e: IOException) {
            notifyPlayStatusChanged(false)
        }
    }

    //add to playlist
    fun insertnext(index: Int, value: Song) {
        //var queue = Queue(mPlayList!!.songs)
        var playIndex = index + 1
        var body = mPlayList!!.songs as ArrayList
        var queue = ArrayList<Song>()
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
        mPlayList!!.setSongs(queue)
        NowPlaying.instance!!.setUpdate()
    }

    fun insertnext(index: Int, value: ArrayList<Song>) {
        //var queue = Queue(mPlayList!!.songs)
        var playIndex = index + 1
        var body = mPlayList!!.songs as ArrayList
        var queue = ArrayList<Song>()
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
        mPlayList!!.setSongs(queue)
        NowPlaying.instance!!.setUpdate()
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

    //Audio Focus
    override fun onAudioFocusChange(focusChange: Int) {
        //
    }

    companion object {

        private val TAG = "Player"

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
