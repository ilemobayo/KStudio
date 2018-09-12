package com.musicplayer.aow.delegates.softcode.adapters

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.ReloadEvent
import com.musicplayer.aow.utils.CursorDB
import java.io.File

class SongFunctions{

    private var trackDatabase = TrackDatabase.getsInstance(Injection.provideContext()!!)

    private val context = Injection.provideContext()
    private val MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val WHERE = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
            + MediaStore.Audio.Media.SIZE + ">0" )
    private val ORDER_BY = MediaStore.Audio.Media.TITLE + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Media.DATA, // the real path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE)
    var tracks: ArrayList<Track> = ArrayList()

    fun getSongs(completion: (ArrayList<Track>, Error?) -> Unit){
        val data = context!!.contentResolver.query(MEDIA_URI,
                PROJECTIONS, WHERE, null,
                ORDER_BY)
        tracks = ArrayList()
        if (data != null) {
            while (data.moveToNext()) {
                tracks.add(CursorDB().cursorToMusic(data))
            }
        }
        data.close()
        completion(tracks, null)
    }

    fun deletSongFromPhone(context: Context, model: Track){
        val file = File(model.path)
        if (file.exists()){
            if (file.delete()) {
                var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                mFilePath += model.path
                val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                AppExecutors.instance?.diskIO()?.execute {
                    trackDatabase?.trackDAO()?.deleteTrack(model)
                }
                context.contentResolver.delete( rootUri,
                        MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
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
            AppExecutors.instance?.diskIO()?.execute {
                trackDatabase?.trackDAO()?.deleteTrack(model)
            }
            context.contentResolver.delete( rootUri,
                    MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
        }
    }


}