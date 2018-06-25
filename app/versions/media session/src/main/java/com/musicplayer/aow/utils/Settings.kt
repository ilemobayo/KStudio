package com.musicplayer.aow.utils

import android.app.Activity
import android.app.Application
import android.os.Environment
import android.util.Log
import com.github.nisrulz.sensey.FlipDetector
import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.player.Player
import java.io.File
import java.io.FilenameFilter
import java.util.*




/**
 * Created by Arca on 11/23/2017.
 */
class Settings (): Application() {

    var context: Activity? = null
    var mPlayer: Player? = Player.instance
    var flipGesture = flipListener()
    var shakeGesture = shakeListner()
    val shakeaction = "shake"
    val flipaction = "flip"

    fun intialization(){
        Sensey.getInstance().init(Injection.provideContext()!!)
        var getSettings: StorageUtil? = StorageUtil(Injection.provideContext()!!)
        //Shake Gesture settings
        var shakeSettings = getSettings!!.loadStringValue(shakeaction)
        Sensey.getInstance().init(Injection.provideContext()!!);
        if (shakeSettings.equals("on")){
            getSettings!!.saveStringValue(shakeaction, "on")
            Sensey.getInstance().startShakeDetection(shakeGesture);
        }else{
            getSettings!!.saveStringValue(shakeaction, "off")
            Sensey.getInstance().stopShakeDetection(shakeGesture);
        }
        //Flip gesture settings
        var flipSettings = getSettings!!.loadStringValue(shakeaction)
        Sensey.getInstance().init(Injection.provideContext()!!);
        if (flipSettings.equals("on")){
            getSettings!!.saveStringValue(flipaction, "on")
            Sensey.getInstance().startFlipDetection(flipGesture);
        }else{
            getSettings!!.saveStringValue(flipaction, "off")
            Sensey.getInstance().stopFlipDetection(flipGesture);
        }
    }

    fun shakeListner(): ShakeDetector.ShakeListener {
        //Player instance
        val shakeListener = object : ShakeDetector.ShakeListener {
            override fun onShakeDetected() {
                if (mPlayer != null) {
                    if (mPlayer!!.isPlaying) {
                        mPlayer!!.playNext()
                    }
                }
            }
            override fun onShakeStopped() {
                // Shake stopped, do something
            }

        }
        return shakeListener
    }

    fun flipListener(): FlipDetector.FlipListener {
        //Sensey Flip Gesture
        val flipListener = object : FlipDetector.FlipListener {
            override fun onFaceUp() {
                if (mPlayer != null) {
                    if (!mPlayer!!.isPlaying) {
                        mPlayer!!.play()
                    }
                }
            }
            override fun onFaceDown() {
                if (mPlayer != null) {
                    if (mPlayer!!.isPlaying) {
                        mPlayer!!.pause()
                    }
                }
            }
        }
        return flipListener
    }

    fun searchSongs(): Boolean {
        var songModelData: ArrayList<Song> = ArrayList()
//        Log.e("Settings Search Audio", "transfered")
        //context or activity
        var songCursor = CursorDB.instance!!.callCursor(Injection.provideContext()!!)
        var albumCursor = CursorDB.instance!!.albumaCursor(Injection.provideContext()!!)
        if (songCursor != null) {
            var indexPosition = 0
            while (songCursor != null && songCursor!!.moveToNext()) {
                indexPosition += 1
//                songModelData.add(CursorDB().cursorToMusic(songCursor!!, albumCursor!!,indexPosition))
            }
            songCursor!!.close()
        }
        return StorageUtil(Injection.provideContext()!!).storeAudio(songModelData)
    }

    fun searchSongsLocal(): Boolean {
        var dir = File(Environment.getExternalStorageDirectory().absolutePath)
        var songList: ArrayList<Song>? = null
        var songModelData = getfile(dir, songList)
        return StorageUtil(Injection.provideContext()!!).storeAudio(songModelData)
    }

    fun getfile(dir: File, songList: ArrayList<Song>?): ArrayList<Song>? {
        var fileList:ArrayList<File>? = null
        var listFile = dir.listFiles()

        if (listFile != null && listFile.size > 0) {
            for (i in listFile.indices) {

                if (listFile[i].isDirectory) {
                    fileList?.add(listFile[i])
                    getfile(listFile[i], songList)

                } else {
                    if (listFile[i].name.endsWith(".mp3")) {
                        fileList?.add(listFile[i])
                        var song = FileUtilities.fileToMusic(File(listFile[i].toURI()))
                        Log.e("Settings.kt",song.toString())
                        songList?.add(song!!)
                        Log.e("Settings.kt",songList?. toString())
                    }
                    Log.e("Settings.kt",songList?.size.toString())
                }

            }
        }
        Log.e("Settings.kt",songList?.size.toString())
        return songList
    }



    var songsList: ArrayList<Song?>? = null

    fun getSongsLocally(): ArrayList<Song?>? {
        val home = File(Environment.getExternalStorageDirectory().absolutePath)

        if (home.listFiles(FileExtensionFilter()).isNotEmpty()) {
            for (file in home.listFiles(FileExtensionFilter())) {
//                val song = HashMap<String, String>()
//                song.put("songTitle", file.getName().substring(0, file.getName().length - 4))
//                song.put("songPath", file.getPath())
                var song = FileUtilities.fileToMusic(File(file.toURI()))
                // Adding each song to SongList
                songsList?.add(song)
                Log.e("Settings.kt plus",songsList?.size.toString())
            }
        }
        Log.e("Settings.kt Total",songsList?.size.toString())
        // return songs list array
        return songsList
    }

    internal inner class FileExtensionFilter : FilenameFilter {
        override fun accept(dir: File, name: String): Boolean {
            return name.endsWith(".mp3") || name.endsWith(".MP3")
        }
    }

    fun readSongs(): ArrayList<Song>?{
        var getSettings = StorageUtil(Injection.provideContext()!!)
        if (getSettings != null) {
            var getAudios = getSettings!!.loadAudio()
            return getAudios
        }
        return ArrayList(0)
    }



    companion object {

        private val TAG = "Setings"

        @Volatile private var sInstance: Settings? = null


        val instance: Settings?
            get() {
                if (sInstance == null) {
                    synchronized(Settings.Companion) {
                        if (sInstance == null) {
                            sInstance = Settings()
                        }
                    }
                }
                return sInstance
            }
    }

}