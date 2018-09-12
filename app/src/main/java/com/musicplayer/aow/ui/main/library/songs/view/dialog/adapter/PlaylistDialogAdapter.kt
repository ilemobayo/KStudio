package com.musicplayer.aow.ui.main.library.songs.view.dialog.adapter

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
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import org.jetbrains.anko.find





/**
 * Created by Arca on 1/23/2018.
 */
class PlaylistDialogAdapter (
        playlist: List<PlayList>,
        private var track: Track,
        private var tracks: ArrayList<Track>? = ArrayList(),
        private var dialog: BottomSheetDialog,
        private var arrayOfSongs: Boolean = false): RecyclerView.Adapter<PlaylistDialogAdapter.PlayListViewHolder>() {

    private var playlisDatabase: PlaylistDatabase? = PlaylistDatabase.getsInstance(Injection.provideContext()!!)
    val TAG = "PlayListAdapter"
    private val mSongModel = playlist

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        val model = mSongModel[position]
        val playlistName = model.name
        val playlistdetails = model.numOfSongs
        holder.pName.text = playlistName
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
                model.addSong(track)
                updatePlayList(model)
            }else{
                tracks?.forEach {
                    model.addSong(it)
                }
                updatePlayList(model)
            }
            //close parent dialog
            dialog.dismiss()
        }

        //here we set item click for tracks
        //to set options
    }

    private fun updatePlayList(playlist: PlayList) {
        AppExecutors.instance?.diskIO()?.execute{
            playlisDatabase?.playlistDAO()?.updatePlayList(playlist)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        return PlayListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_dialog_playlist,parent,false))
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