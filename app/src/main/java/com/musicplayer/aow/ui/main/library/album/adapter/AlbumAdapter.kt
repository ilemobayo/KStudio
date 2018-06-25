package com.musicplayer.aow.ui.main.library.album.adapter

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.musicplayer.aow.R
import com.musicplayer.aow.R.id.menu_item_add_to_queue
import com.musicplayer.aow.R.id.menu_item_play_next
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.utils.images.BitmapDraws
import org.jetbrains.anko.find
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 * Created by Arca on 11/20/2017.
 */
class AlbumAdapter(var context: Context, var activity: Activity, albumList: ArrayList<Album>?) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    private var view:View? = null
    private val mSubscriptions: CompositeSubscription? = null
    var mAlbumModel = albumList

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val model = mAlbumModel?.get(position)
        holder!!.albumName.text = model?.albumName
        holder.albumArtist.text = model?.artist

        if (model?.albumArt != null || model?.albumArt != "null") {
            val albumArt = BitmapDraws.createFromPath(model?.albumArt)
            if (albumArt != null) {
                holder.albumArt.setImageDrawable(albumArt)
            }else{
                holder.albumArt.setImageResource(R.drawable.gradient_danger)
            }
        } else{
            holder.albumArt.setImageResource(R.drawable.gradient_danger)
        }

        holder.cardView.setOnClickListener {
            SoftCodeAdapter().openAlbumActivity(context, model!!)
        }

        //to set options
        holder.option.setOnClickListener {
            val songs = SoftCodeAdapter().getAlbumTracks(context, model!!.album_id!!, true)
            if (holder.view != null) {
                val context = holder.view.context
                val mBottomSheetDialog = BottomSheetDialog(context)
                val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_album, null)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()
                mBottomSheetDialog.setOnDismissListener {
                    //perform action on close
                }

                val play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
                val playNext = sheetView.find<LinearLayout>(menu_item_play_next)
                val queue = sheetView.find<LinearLayout>(menu_item_add_to_queue)
                val playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
                val delete = sheetView.find<LinearLayout>(R.id.menu_item_delete)

                play.setOnClickListener {
                    RxBus.instance!!.post(PlayListNowEvent(PlayList(songs), 0))
                    mBottomSheetDialog.dismiss()
                }

                playNext.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.playingIndex,songs)
                    mBottomSheetDialog.dismiss()
                }

                queue.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,songs)
                    mBottomSheetDialog.dismiss()
                }

                playlist.setOnClickListener {
                    mBottomSheetDialog.dismiss()
                    //Dialog with ListView
                    val nContext = view!!.context
                    val mSelectPlaylistDialog = BottomSheetDialog(nContext)
                    val sheetView =  LayoutInflater.from(nContext).inflate(R.layout.custom_dialog_select_playlist, null)
                    val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                    SoftCodeAdapter().addSongToPlaylist(activity,nContext, mylist, mSelectPlaylistDialog, Song(), songs, true)

                    mSelectPlaylistDialog.setContentView(sheetView)
                    mSelectPlaylistDialog.show()
                    mSelectPlaylistDialog.setOnDismissListener {}
                }

                delete.setOnClickListener {
                    SoftCodeAdapter().deleteAlbum(context, model.album_id!!, true)
                    mBottomSheetDialog.dismiss()
                }
            }
        }
    }

    fun swapCursor(albumList: ArrayList<Album>?): ArrayList<Album>? {
        if (mAlbumModel === albumList) {
            return null
        }
        val oldCursor = mAlbumModel
        this.mAlbumModel = albumList
        if (albumList != null) {
            this.notifyDataSetChanged()
        }
        return oldCursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.container_fish,parent,false)
        return AlbumViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mAlbumModel?.size!!
    }

    class AlbumViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var albumName: TextView = itemView.find(R.id.cardname)
        var albumArtist: TextView = itemView.find(R.id.cardart)
        var albumArt: ImageView = itemView.find(R.id.ivFish)
        var cardView: LinearLayout = itemView.find(R.id.card_view_container)
        var option: AppCompatImageView = itemView.find(R.id.item_button_action)
        var view: View = itemView
    }

}