package com.musicplayer.aow.delegates.data.cache

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by Arca on 4/1/2018.
 * https://github.com/kittinunf/Fuel
 * https://github.com/kittinunf/Fuse
 */
class AudioCache {

    fun saveToFile(data: ByteArray, name: String){
        try {
            //StorageUtil(Injection.provideContext()!!.applicationContext).byteArrayToFile(data, name)
            val file = File(Environment.getExternalStorageDirectory(), "musixplay/cache/.audio/$name.mxp")
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.close()
        } catch (e: IOException) {
            Log.e("AudoCache Error", "Error write " + e)
            e.printStackTrace()
        }
    }

    fun readFromFile(name: String): ByteArray? {
        try {
            val file = File(Environment.getExternalStorageDirectory(), "musixplay/cache/.audio/$name.mxp")
            var inputStream: InputStream = file.inputStream()
            var inputString = inputStream.bufferedReader().use { it.readText()}
            return inputString.toByteArray()
        }catch (e: IOException){
            Log.e("AudoCache Error", "Error read")
            e.printStackTrace()
            return null
        }
    }

}