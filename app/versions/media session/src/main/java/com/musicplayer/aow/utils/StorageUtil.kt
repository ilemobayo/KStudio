package com.musicplayer.aow.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.model.TempSongs
import com.musicplayer.aow.delegates.data.source.AppRepository
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileOutputStream
import java.util.*


class StorageUtil(context: Context) {

    var applicationFoldercontext = "musixplay"

    var context = Injection.provideContext()!!

    val STORAGE = "com.musicplayer.aow.STORAGE"
    private var preferences: SharedPreferences? = null

    private val mRepository: AppRepository = AppRepository.instance!!
    private val mSubscriptions: CompositeSubscription? = null


    fun appPreference(): SharedPreferences? {
        return context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
    }

    fun storeAudio(arrayList: ArrayList<Song>?): Boolean {
        preferences = context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("songs", json)
        editor.apply()
        return true
    }

    fun loadAudio(): ArrayList<Song>? {
        preferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        val gson = Gson()
        val json = preferences!!.getString("songs", null)
        val type = object : TypeToken<ArrayList<Song>>() {}.type
        return if (json.isNullOrEmpty()) {
            ArrayList()
        }else {
            gson.fromJson<ArrayList<Song>>(json, type)
        }
    }

    fun storeAudioIndex(index: Int) {
        preferences = this.context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        val editor = preferences!!.edit()
        editor.putInt("audioIndex", index)
        editor.apply()
    }

    fun loadAudioIndex(): Int {
        preferences = this.context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        if (preferences != null) {
            return preferences!!.getInt("audioIndex", -1)//return -1 if no data found
        }else{
            return 0
        }
    }

    fun clearCachedAudioPlaylist() {
        preferences = this.context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        val editor = preferences!!.edit()
        editor.clear()
        editor.commit()
    }

    fun saveStringValue(name: String, value: String){
        preferences = this.context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        val editor = preferences!!.edit()
        editor.putString(name, value)
        editor.apply()
    }

    fun loadStringValue(name: String): String? {
        preferences = this.context!!.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        return preferences!!.getString(name, "empty")
    }

    private fun updateAllSongsPlayList(playList: PlayList) {
        val subscription = mRepository.setInitAllSongs(playList).subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() { }

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                        //Log.e("All Songs Size", result.numOfSongs.toString())
                    }
                })
        mSubscriptions?.add(subscription)
    }

    fun refreshDatabase(){
        TempSongs.instance!!.setSongs()
        updateAllSongsPlayList(playList = PlayList(TempSongs.instance!!.songs))
    }

    fun storageLocationDir(){
        val mediaStorageDir = File(Environment.getExternalStorageDirectory(), applicationFoldercontext)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("App", "failed to create directory")
            }else{
                var folderNames = arrayListOf("/tmpImg", "/update", "/cache/.img","/cache/.audio", "/.media", "/.nomedia",
                        "/download/lyrics", "/download/audio", "/download/albumart","/items", "/data")
                folderNames
                        .asSequence()
                        .map { File(Environment.getExternalStorageDirectory(), applicationFoldercontext + it) }
                        .filter { !it.exists() && !it.mkdirs() }
                        .forEach { Log.e("App", "failed to create directory") }
            }
        }
    }

    fun byteArrayToFile(byteArray: ByteArray, name: String): String? {
        val file = File(Environment.getExternalStorageDirectory(), applicationFoldercontext + "/cache/.audio/" + name + ".mxp")
        if (!file.exists()) {
            file.createNewFile()
        }
        val fos = FileOutputStream(file)
        fos.write(byteArray)
        fos.close()
        return file.absolutePath
    }
}
