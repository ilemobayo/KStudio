package com.musicplayer.aow.ui.main.library.songs.dialog.adapter

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.ui.main.library.playlist.PlayListAction
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * Created by Arca on 3/25/2018.
 */
class PlaylistDialogSLAdapter (context: Context, playlist: List<PlayList>, songs: PlayList, dialog: BottomSheetDialog): RecyclerView.Adapter<PlaylistDialogSLAdapter.PlayListViewHolder>() {

    val TAG = "PlayListAdapter"
    var context = context.applicationContext
    private val mSongModel = playlist
    private val mRepository: AppRepository? = AppRepository.instance
    private val mSubscriptions: CompositeSubscription? = null
    private var view: View? = null
    private var dialog = dialog
    private var songs = songs

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: PlayListViewHolder?, position: Int) {
        var model = mSongModel[position]
        var playlistName = model.name
        var playlistdetails = model.numOfSongs
        holder!!.pName.text = playlistName
        holder.pDetails.text = playlistdetails.toString()

        //Drawable Text
        var generator = ColorGenerator.MATERIAL // or use DEFAULT
        // generate random color
        var color1 = generator.randomColor
        var icon = TextDrawable.builder().beginConfig()
                .width(55)  // width in px
                .height(55) // height in px
                .endConfig().buildRect(playlistName!!.substring(0,1), color1)
        holder.albumArt!!.setImageDrawable(icon)

        //implementation of item click
        holder.item.setOnClickListener {
            //add to playlist here
            model.addSong(songs.songs, model.numOfSongs)
            updatePlayList(model)
            //close parent dialog
            dialog.dismiss()
        }

        //here we set item click for songs
        //to set options
    }

    private fun updatePlayList(playList: PlayList) {
        val subscription = mRepository?.update(playList)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(playList: PlayList) {}
                })
        //Update UI
        RxBus.instance!!.post(PlayListAction(true))
        mSubscriptions?.add(subscription)
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PlayListViewHolder? {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_dialog_playlist,parent,false)
        return PlayListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
    }

    class PlayListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var pName: TextView = itemView.find<TextView>(R.id.text_view_name)
        var pDetails: TextView = itemView.find<TextView>(R.id.text_view_info)
        var albumArt = itemView.findViewById<ImageView>(R.id.image_view_album)
        var item: RelativeLayout = itemView.find<RelativeLayout>(R.id.item)

    }

}