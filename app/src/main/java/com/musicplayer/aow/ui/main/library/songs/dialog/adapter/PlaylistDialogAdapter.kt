package com.musicplayer.aow.ui.main.library.songs.dialog.adapter

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import org.jetbrains.anko.find





/**
 * Created by Arca on 1/23/2018.
 */
class PlaylistDialogAdapter (
        context: Context,
        playlist: List<PlayList>,
        private var song: Song,
        private var songs: ArrayList<Song>? = ArrayList(),
        private var dialog: BottomSheetDialog,
        private var arrayOfSongs: Boolean = false): RecyclerView.Adapter<PlaylistDialogAdapter.PlayListViewHolder>() {

    val TAG = "PlayListAdapter"
    var context: Context = context.applicationContext
    private val mSongModel = playlist
    private var view: View? = null

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        val model = mSongModel[position]
        val playlistName = model.name
        val playlistdetails = model.numOfSongs
        holder!!.pName.text = playlistName
        //holder.pDetails.text = playlistdetails.toString()

        //Drawable Text
        val generator = ColorGenerator.MATERIAL // or use DEFAULT
        // generate random color
        val color1 = generator.randomColor
        val icon = TextDrawable.builder().beginConfig()
                .width(55)  // width in px
                .height(55) // height in px
                .endConfig().buildRect(playlistName!!.substring(0,1), color1)
        holder.albumArt.setImageDrawable(icon)

        //implementation of item click
        holder.item.setOnClickListener {
            //add to playlist here
            if (!arrayOfSongs) {
                Log.e(this.javaClass.name, "single")
                model.addSong(song)
                updatePlayList(model._id, song.id)
            }else{
                Log.e(this.javaClass.name, "list of ${songs?.size} songs")
                songs?.forEach {
                    model.addSong(it)
                    updatePlayList(model._id, it.id)
                }
            }
            //close parent dialog
            dialog.dismiss()
        }

        //here we set item click for songs
        //to set options
    }

    private fun updatePlayList(playListId: Long, songId: Int) {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId)
        val cursor = context.contentResolver.query(uri, arrayOf("count(*)"), null, null, null)
        cursor.moveToFirst()
        var last = cursor.getInt(0)
        cursor.close()
        val value = ContentValues()
        value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, ++last)
        value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId)
        context.contentResolver.insert(uri, value);
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_dialog_playlist,parent,false)
        return PlayListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
    }

    class PlayListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var pName: TextView = itemView.find(R.id.text_view_name)
        var pDetails: TextView = itemView.find(R.id.text_view_info)
        var albumArt: ImageView = itemView.findViewById(R.id.image_view_album)
        var item: RelativeLayout = itemView.find(R.id.item)
    }

}