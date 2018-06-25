package com.musicplayer.aow.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.Log
import com.github.nisrulz.sensey.FlipDetector
import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector
import com.github.tbouron.shakedetector.library.ShakeDetector.OnShakeListener
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.utils.FileUtilities.fileToMusic
import org.jetbrains.anko.doAsync
import java.io.File
import java.util.*


/**
 * Created by Arca on 11/23/2017.
 */
class ApplicationSettings(): Application() {

    var context: Activity? = null
    var mPlayer: Player? = Player.instance
    var flipGesture = flipListener()
    var shakeGesture = shakeListner()
    val shakeaction = "shake"
    val flipaction = "flip"
    val cacheaction = "cache"
    val file_server_action = "server"

    fun intialization(context: Activity){
        this.context = context
        doAsync {
            var getSettings: StorageUtil? = StorageUtil(Injection.provideContext()!!)
            //Shake Gesture settings
            var shakeSettings = getSettings!!.loadStringValue(shakeaction)
            ShakeWithSensorDetector()
            Sensey.getInstance().init(Injection.provideContext());
            if (shakeSettings.equals("on")) {
                getSettings!!.saveStringValue(shakeaction, "on")
                ShakeWithSensorDetectorResume()
                Sensey.getInstance().startShakeDetection(shakeGesture);
            } else {
                getSettings!!.saveStringValue(shakeaction, "off")
                ShakeWithSensorDetectorStop()
                Sensey.getInstance().stopShakeDetection(shakeGesture);
            }
            //Flip gesture settings
            var flipSettings = getSettings!!.loadStringValue(shakeaction)
            Sensey.getInstance().init(Injection.provideContext());
            if (flipSettings.equals("on")) {
                getSettings!!.saveStringValue(flipaction, "on")
                Sensey.getInstance().startFlipDetection(flipGesture);
            } else {
                getSettings!!.saveStringValue(flipaction, "off")
                Sensey.getInstance().stopFlipDetection(flipGesture);
            }
        }
    }

    fun ShakeWithSensorDetector(){
        com.github
                .tbouron
                .shakedetector
                .library
                .ShakeDetector.create(MusicPlayerApplication!!.instance, OnShakeListener {
            if (mPlayer != null) {
                if (mPlayer!!.isPlaying) {
                    mPlayer!!.playNext()
                }
            }
        })
    }

    fun ShakeWithSensorDetectorStop(){
        com.github
                .tbouron
                .shakedetector
                .library
                .ShakeDetector.stop();
    }

    fun ShakeWithSensorDetectorResume(){
        com.github
                .tbouron
                .shakedetector
                .library
                .ShakeDetector.start()
    }

    fun ShakeWithSensorDetectorDestroy(){
        com.github
                .tbouron
                .shakedetector
                .library
                .ShakeDetector.destroy()
    }

    fun ShakeWithSensorDetectorUpdate(sensibility: Float, numberOfShake: Int){
        com.github
                .tbouron
                .shakedetector
                .library
                .ShakeDetector.updateConfiguration(sensibility, numberOfShake);
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

    fun searchSongsLocal(context: Context): Boolean {
        //context or activity
        var dir = File(Environment.getExternalStorageDirectory().absolutePath)
        var songModelData = getfile(dir)
        return StorageUtil(context).storeAudio(songModelData)
    }

    fun searchSongsLocal(): ArrayList<Song>? {
        //context or activity
        var dir = File(Environment.getExternalStorageDirectory().absolutePath)
        var songModelData = getfile(dir)
        return songModelData
    }

    var a = 0
    var songList: ArrayList<Song>? = null
    fun getfile(dir: File): ArrayList<Song>? {
        var fileList:ArrayList<File>? = null
        val listFile = dir.listFiles()

        if (listFile != null && listFile.size > 0) {
            for (i in listFile.indices) {

                if (listFile[i].isDirectory) {
                    fileList?.add(listFile[i])
                    getfile(listFile[i])

                } else {
                    if (listFile[i].name.endsWith(".mp3")) {
                        fileList?.add(listFile[i])
                        var song = fileToMusic(File(listFile[i].toURI()))
                        songList?.add(song!!)
                        a = a+1
                        Log.e("Application.kt","$a "+ songList?.size)
                    }
                }

            }
        }
//        songList?.forEach { e ->  Log.e("Application.kt",e.toString())}
        return songList
    }

    fun searchSongs(context: Context): Boolean {
        var songModelData: ArrayList<Song> = ArrayList()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        //context or activity
        var songCursor = CursorDB().callCursor(context)
        var albumCursor = CursorDB.instance!!.albumaCursor(Injection.provideContext()!!)
        if (songCursor != null) {
            var indexPosition = 0
            //clear albumart cache
//            File(Environment.getExternalStorageDirectory(), context.packageName + "/cache/.img/").listFiles {
//                e ->  e.delete()
//            }
            while (songCursor != null && songCursor!!.moveToNext()) {
                indexPosition = indexPosition + 1
//                songModelData.add(CursorDB().cursorToMusic(songCursor!!,albumCursor!!, indexPosition))
                //save album art to app folder
//                val metadataRetriever = MediaMetadataRetriever()
//                metadataRetriever.setDataSource(songModelData[indexPosition - 1].path)
//                val albumData = metadataRetriever.embeddedPicture
//                if(albumData != null){
//                    songModelData[indexPosition - 1].albumArt = albumData.toString()
//                    songModelData[indexPosition - 1].albumArt = StorageUtil(context).byteArrayToFile(albumData.clone(), SystemClock.currentThreadTimeMillis().toString())
//                }
            }
            songCursor!!.close()
        }else{
            //
        }
        return StorageUtil(context).storeAudio(songModelData)
    }

    fun readSongs(): ArrayList<Song>?{
        var getSettings = StorageUtil(Injection.provideContext()!!)
        if (getSettings != null) {
            var getAudios = getSettings!!.loadAudio()
            return getAudios
        }
        return ArrayList(0)
    }

    fun readSongs(context: Context): ArrayList<Song>?{
        var getSettings = StorageUtil(Injection.provideContext()!!)
        if (getSettings != null) {
            var getAudios = getSettings?.loadAudio()
            return getAudios
        }
        return ArrayList(0)
    }

    companion object {

        private val TAG = "Setings"

        private val MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private val WHERE = MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + MediaStore.Audio.Media.SIZE + ">0"
        private val ORDER_BY = MediaStore.Audio.Media.DISPLAY_NAME + " ASC"
        private val PROJECTIONS = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.IS_RINGTONE, MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media.IS_NOTIFICATION, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID)
        private val ALBUM_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        private val WHERE_ALBUM = MediaStore.Audio.Albums._ID + " = ?"
        private val PROJECTIONS_ALBUM = arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_KEY, MediaStore.Audio.Albums.ALBUM_ART)


        @Volatile private var sInstance: ApplicationSettings? = null

        val instance: ApplicationSettings?
            get() {
                if (sInstance == null) {
                    synchronized(ApplicationSettings.Companion) {
                        if (sInstance == null) {
                            sInstance = ApplicationSettings()
                        }
                    }
                }
                return sInstance
            }
    }

}