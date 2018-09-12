package com.musicplayer.aow.utils

import android.app.Activity
import android.os.Environment
import android.util.Log
import com.github.nisrulz.sensey.FlipDetector
import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.player.Player
import java.io.File
import java.io.FilenameFilter
import java.util.*




/**
 * Created by Arca on 11/23/2017.
 */
class Settings {

    var context: Activity? = null
    var mPlayer: Player? = Player.instance
    var flipGesture = flipListener()
    var shakeGesture = shakeListner()
    val shakeaction = "shake"
    val flipaction = "flip"
    var getSettings: StorageUtil? = null
    var shakeSettings: String? = "off"
    var flipSettings: String? = "off"

    fun intialization(){
        Sensey.getInstance().init(Injection.provideContext()!!)
        getSettings = StorageUtil(Injection.provideContext()!!)
        shakeSettings = getSettings?.loadStringValue(shakeaction)
        flipSettings = getSettings?.loadStringValue(flipaction)
        //Shake Gesture settings
        Sensey.getInstance().init(Injection.provideContext()!!)
        Sensey.getInstance().startShakeDetection(shakeGesture)
        Sensey.getInstance().startFlipDetection(flipGesture)
    }

    private fun shakeListner(): ShakeDetector.ShakeListener {
        //Player instance
        return object : ShakeDetector.ShakeListener {
            override fun onShakeDetected() {
                if (shakeSettings.equals("on")) {
                    if (mPlayer != null) {
                        if (mPlayer!!.isPlaying) {
                            mPlayer!!.playNext()
                        }
                    }
                }
            }
            override fun onShakeStopped() {
                // Shake stopped, do something
            }

        }
    }

    private fun flipListener(): FlipDetector.FlipListener {
        //Sensey Flip Gesture
         return object : FlipDetector.FlipListener {
            override fun onFaceUp() {
                if (flipSettings.equals("on")) {
                    if (mPlayer != null) {
                        if (!mPlayer!!.isPlaying) {
                            mPlayer!!.play()
                        }
                    }
                }
            }
            override fun onFaceDown() {
                if (flipSettings.equals("on")) {
                    if (mPlayer != null) {
                        if (mPlayer!!.isPlaying) {
                            mPlayer!!.pause()
                        }
                    }
                }
            }
        }
    }

    fun getfile(dir: File, trackList: ArrayList<Track>?): ArrayList<Track>? {
        val fileList:ArrayList<File>? = null
        val listFile = dir.listFiles()

        if (listFile != null && listFile.isNotEmpty()) {
            for (i in listFile.indices) {

                if (listFile[i].isDirectory) {
                    fileList?.add(listFile[i])
                    getfile(listFile[i], trackList)

                } else {
                    if (listFile[i].name.endsWith(".mp3")) {
                        fileList?.add(listFile[i])
                        val song = FileUtilities.fileToMusic(File(listFile[i].toURI()))
                        Log.e("Settings.kt",song.toString())
                        trackList?.add(song!!)
                        Log.e("Settings.kt",trackList?. toString())
                    }
                    Log.e("Settings.kt",trackList?.size.toString())
                }

            }
        }
        Log.e("Settings.kt",trackList?.size.toString())
        return trackList
    }



    var songsList: ArrayList<Track?>? = null

    fun getSongsLocally(): ArrayList<Track?>? {
        val home = File(Environment.getExternalStorageDirectory().absolutePath)

        if (home.listFiles(FileExtensionFilter()).isNotEmpty()) {
            for (file in home.listFiles(FileExtensionFilter())) {
                val song = FileUtilities.fileToMusic(File(file.toURI()))
                // Adding each track to SongList
                songsList?.add(song)
                Log.e("Settings.kt plus",songsList?.size.toString())
            }
        }
        Log.e("Settings.kt Total",songsList?.size.toString())
        // return tracks list array
        return songsList
    }

    internal inner class FileExtensionFilter : FilenameFilter {
        override fun accept(dir: File, name: String): Boolean {
            return name.endsWith(".mp3") || name.endsWith(".MP3")
        }
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