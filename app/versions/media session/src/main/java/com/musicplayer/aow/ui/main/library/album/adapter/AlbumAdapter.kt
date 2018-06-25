package com.musicplayer.aow.ui.main.library.album.adapter

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.musicplayer.aow.R
import com.musicplayer.aow.R.id.menu_item_add_to_queue
import com.musicplayer.aow.R.id.menu_item_play_next
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.objects.AlbumLists
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.main.library.activities.AlbumSongs
import com.musicplayer.aow.ui.main.library.songs.dialog.adapter.PlaylistDialogSLAdapter
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 * Created by Arca on 11/20/2017.
 */
class AlbumAdapter(context: Context,activity: Activity,playList: ArrayList<AlbumLists>) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    private var view:View? = null
    private val mSubscriptions: CompositeSubscription? = null
    val mSongModel = playList
    var context = context
    var activity = activity


    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: AlbumViewHolder?, position: Int) {
        var model = mSongModel[position]
        var albumName = model.albumName
        var albumArtist = model.albumArtist
        holder!!.albumName.text = albumName
        holder.albumArtist.text = albumArtist
        if (model.albumArt != null || model.albumArt != "null") {
            val albumArt = Drawable.createFromPath(model.albumArt)
            if (albumArt != null) {
                holder.albumArt.setImageDrawable(albumArt)
            }else{
                //Drawable Text
                var generator = ColorGenerator.MATERIAL // or use DEFAULT
                // generate random color
                var color1 = generator.randomColor
                var icon = TextDrawable.builder().buildRect(model.albumName!!.substring(0,1), color1)
                holder.albumArt.setImageDrawable(icon)
            }
        }else{
            //Drawable Text
            var generator = ColorGenerator.MATERIAL // or use DEFAULT
            // generate random color
            var color1 = generator.randomColor
            var icon = TextDrawable.builder().buildRect(model.albumName!!.substring(0,1), color1)
            holder.albumArt.setImageDrawable(icon)
        }
        holder.cardView.setOnClickListener {
            val intent = Intent(context, AlbumSongs::class.java).apply {
                putExtra("com.musicplayer.aow.album.name", albumName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(context, intent, null)
        }

        //to set options
        holder.option.setOnClickListener {
            if (holder.view != null) {
                var context = holder.view.context
                val mBottomSheetDialog = BottomSheetDialog(context)
                val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_album, null)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()
                mBottomSheetDialog.setOnDismissListener {
                    //perform action on close
                }

                var play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
                var playNext = sheetView!!.find<LinearLayout>(menu_item_play_next)
                var queue = sheetView!!.find<LinearLayout>(menu_item_add_to_queue)
                var playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)

                play.setOnClickListener {
                    RxBus.instance!!.post(PlayListNowEvent(model.PlayList, 0))
                    mBottomSheetDialog.dismiss()
                }

                playNext.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.playingIndex,model.PlayList.songs as ArrayList<Song>)
                    mBottomSheetDialog.dismiss()
                }

                queue.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,model.PlayList.songs as ArrayList<Song>)
                    mBottomSheetDialog.dismiss()
                }

                playlist.setOnClickListener {
                    mBottomSheetDialog.dismiss()
                    //Dialog with ListView
                    var context = view!!.context
                    val mSelectPlaylistDialog = BottomSheetDialog(context)
                    val sheetView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog_select_playlist, null)
                    var mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                    //load data
                    val subscription = AppRepository().playLists()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : Subscriber<List<PlayList>>() {
                                override fun onStart() {}

                                override fun onCompleted() {}

                                override fun onError(e: Throwable) {}

                                override fun onNext(playLists: List<PlayList>) {
                                    //recycler adapter
                                    var playlistModelData = playLists
                                    //get audio from shearPref
                                    if (playlistModelData != null){
                                        //sort the song list in ascending order
                                        var playList = playlistModelData.sortedWith(compareBy({ (it.name)!!.toLowerCase() }))
                                        //Save to database
                                        var playListAdapter = PlaylistDialogSLAdapter(activity, playList, model.PlayList, mSelectPlaylistDialog)
                                        //Setup layout manager
                                        val layoutManager = PreCachingLayoutManager(activity)
                                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                                        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(activity))
                                        mylist.setHasFixedSize(true)
                                        mylist.layoutManager = layoutManager
                                        mylist.adapter = playListAdapter
                                    }
                                }
                            })
                    mSubscriptions?.add(subscription)

                    mSelectPlaylistDialog.setContentView(sheetView)
                    mSelectPlaylistDialog.show()
                    mSelectPlaylistDialog.setOnDismissListener {}
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AlbumViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.container_fish,parent,false)
        return AlbumViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
    }

    class AlbumViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var albumName: TextView = itemView.find<TextView>(R.id.cardname)
        var albumArtist: TextView = itemView.find<TextView>(R.id.cardart)
        var albumArt: ImageView = itemView.find<ImageView>(R.id.ivFish)
        var cardView: LinearLayout = itemView.find<LinearLayout>(R.id.card_view_container)
        var option: AppCompatImageView = itemView.find<AppCompatImageView>(R.id.item_button_action)
        var view: View = itemView

    }

}