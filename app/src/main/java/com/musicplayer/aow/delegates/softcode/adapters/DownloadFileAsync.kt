package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import android.os.Environment
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import okhttp3.*
import java.io.FileOutputStream
import java.io.IOException

class DownloadFileAsync (val context: Context){

    @Throws(Exception::class)
    fun downloadFileAsync(downloadUrl: String, track: Track) {
        val client = OkHttpClient()
        val request = Request.Builder().url(downloadUrl).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                SoftCodeAdapter().sendNotification(context, "Track Download", "${track.title} download failed.")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    SoftCodeAdapter().sendNotification(context, "Track Download", "Failed to download track/track.")
                } else {
                    val filename = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).absolutePath.plus("/".plus(track.title.plus(".mp3")))
                    val fos = FileOutputStream(filename)
                    fos.write(response.body()?.bytes())
                    fos.close()
                    SoftCodeAdapter()
                            .downloadSuccessNotification(context, "Track Download", filename,"${track.title} downloaded.")
                }
            }

        })
    }

}