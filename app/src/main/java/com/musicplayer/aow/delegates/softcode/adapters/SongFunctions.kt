package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.ReloadEvent
import java.io.File

class SongFunctions{


    fun deletSongFromPhone(context: Context, model: Song){
        val file = File(model.path)
        if (file.exists()){
            if (file.delete()) {
                var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                mFilePath += model.path
                val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                context.contentResolver.delete( rootUri,
                        MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
                RxBus.instance!!.post(ReloadEvent(model))
            } else {
                Toast.makeText(
                        Injection.provideContext(),
                        context.getString(R.string.file_delete_failed),
                        Toast.LENGTH_SHORT).show()
            }
        }else{
            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
            mFilePath += model.path
            val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
            context.contentResolver.delete( rootUri,
                    MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
            RxBus.instance!!.post(ReloadEvent(model))
        }
    }


}