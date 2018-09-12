package com.musicplayer.aow.ui.nowplaying

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.l4digital.fastscroll.FastScroller
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.ui.main.library.activities.ArtistSongs
import com.musicplayer.aow.ui.nowplaying.draganddropinterface.ItemTouchHelperAdapter
import com.musicplayer.aow.ui.nowplaying.draganddropinterface.ItemTouchHelperViewHolder
import com.musicplayer.aow.utils.TimeUtils
import org.jetbrains.anko.find
import java.util.*

/**
 * Created by Arca on 2/7/2018.
 */
class NowPlayingAdapter (
        context: Context,
        song: PlayList = PlayList(),
        private var activity: Activity, dragStartListener: OnDragStartListener) : RecyclerView.Adapter<NowPlayingAdapter.SongListViewHolder>()
        , View.OnClickListener, FastScroller.SectionIndexer, ItemTouchHelperAdapter {

    override fun getSectionText(position: Int): String {
        return mSongModel?.get(position)?.title!!.first().toString()
    }

    private var view: View? = null
    val TAG = "NowPlayingAdapter"
    var context: Context = context.applicationContext
    private var songPlayList = song
    private val mSongModel = song.tracks
    private var mDragStartListener = dragStartListener

    override fun onClick(v: View?) {

    }

    override fun onBindViewHolder(holder: SongListViewHolder, position: Int) {
        val model = mSongModel?.get(position)
        val songArtist = model?.artist
        val tPosition = position
        val tDuration = TimeUtils.formatDuration(model?.duration!!)

        if (Player.instance!!.isPlaying) {
            if (Player.instance!!.playingTrack!!.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                holder.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
                holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
            } else {
                holder.songTV.setTextColor(context.resources.getColor(R.color.black))
                holder.songArtist.setTextColor(context.resources.getColor(R.color.black))
            }
        }

        broadcastChange(holder, model)

        holder.songTV.text = model.title
        holder.songArtist.text = "$tDuration" +  " - " + songArtist


        //implementation of item click
        holder.mListItem!!.setOnClickListener {
            Player.instance?.play(songPlayList, position)
            //notifyItemRangeChanged(position, mSongModel.size)
        }

        holder.reorder.setOnTouchListener { v, event ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onDragStarted(holder)
            }
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                mDragStartListener.onDragStarted(holder)
            }
            true
        }
//        holder.reorder.setOnLongClickListener{
//            it.setOnTouchListener { v, event ->
//                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
//                    mDragStartListener.onDragStarted(holder)
//                }
//                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
//                    mDragStartListener.onDragStarted(holder)
//                }
//                true
//            }
//
//            true
//        }

        //here we set item click for tracks
        //to set options
        holder.option.setOnClickListener {
            if (view != null) {
                val context = view!!.context
                val mBottomSheetDialog = BottomSheetDialog(context)
                val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_all_music, null)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()
                mBottomSheetDialog.setOnDismissListener {
                    //perform action on close
                }

                val play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
                val playNext = sheetView.find<LinearLayout>(R.id.menu_item_play_next)
                val addToQueue = sheetView.find<LinearLayout>(R.id.menu_item_add_to_queue)
                val delete = sheetView.find<LinearLayout>(R.id.menu_item_delete)
                val remove = sheetView.find<TextView>(R.id.delete_label)
                remove.text = context.getString(R.string.remove)
                val album = sheetView.find<LinearLayout>(R.id.menu_item_go_to_album)
                val artist = sheetView.find<LinearLayout>(R.id.menu_item_go_to_artist)
                val playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
                play.setOnClickListener {
                    //Update UI
                    Player.instance?.play(PlayList(mSongModel), position)
                    mBottomSheetDialog.dismiss()
                }
                //play next
                playNext.setOnClickListener {
                    val playingIndex = Player.instance!!.mPlayList!!.playingIndex
                    Player.instance!!.insertnext(playingIndex,model)
                    mBottomSheetDialog.dismiss()
                }
                addToQueue.visibility = View.GONE
                album.setOnClickListener {
                    val sAlbum = SoftCodeAdapter().getAlbum(context, model.albumArt!!)
                    SoftCodeAdapter().openAlbumActivity(context, sAlbum)
                    mBottomSheetDialog.dismiss()
                }
                artist.setOnClickListener {
                    val intent = Intent(context, ArtistSongs::class.java)
                    intent.putExtra("com.musicplayer.aow.artist.name", model.artist)
                    ContextCompat.startActivity(context, intent, null)
                    mBottomSheetDialog.dismiss()
                }
                //Add to Playlist Operation
                playlist.setOnClickListener {
                    mBottomSheetDialog.dismiss()
                    val context = view!!.context
                    val mSelectPlaylistDialog = BottomSheetDialog(context)
                    val sheetView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog_select_playlist, null)
                    val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                    SoftCodeAdapter().addSongToPlaylist(context, mylist, mSelectPlaylistDialog, model)

                    mSelectPlaylistDialog.setContentView(sheetView)
                    mSelectPlaylistDialog.show()
                    mSelectPlaylistDialog.setOnDismissListener {}
                }
                //Delete Operation
                delete.setOnClickListener{
                    removeAt(position, model)
                    mBottomSheetDialog.dismiss()
                }
            }
        }
    }

     fun onItemDismiss(position: Int) {
        mSongModel?.removeAt(position)
         notifyItemRemoved(position)
     }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(mSongModel, fromPosition, toPosition)
        Player.instance?.updatePlaylist(mSongModel as ArrayList<Track>)
        notifyItemMoved(fromPosition, toPosition)
        notifyDataSetChanged()
        return false
    }

    override fun onDrop(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    interface OnDragStartListener {
        fun onDragStarted(viewHolder: RecyclerView.ViewHolder)
    }


    private fun removeAt(position: Int, track: Track) {
        mSongModel?.remove(track)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mSongModel?.size!!)
    }

    private fun broadcastChange(holder: SongListViewHolder?, model: Track){
//        if (mSubscriptions == null) {
//            mSubscriptions = CompositeSubscription()
//        }
//        mSubscriptions!!.add(
//                RxBus.instance?.toObservable()
//                        ?.observeOn(AndroidSchedulers.mainThread())
//                        ?.doOnNext({ o ->
//                            if (o is ChangePlaystate) {
//                                if (o != false) {
//                                    if (Player.instance!!.isPlaying) {
//                                        if (Player.instance!!.playingTrack!!.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
//                                            holder!!.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
//                                            holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
//                                        } else {
//                                            holder!!.songTV.setTextColor(context.resources.getColor(R.color.black))
//                                            holder.songArtist.setTextColor(context.resources.getColor(R.color.black))
//                                        }
//                                    }
//                                }
//                            }
//                        })?.subscribe(RxBus.defaultSubscriber())!!
//        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_nowplaying, parent, false)
        return SongListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel?.size!!
    }

    class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {
        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.WHITE)
        }

        var songTV: TextView = itemView.find(R.id.text_view_name)
        var songArtist: TextView = itemView.find(R.id.text_view_artist)
        var reorder: ImageView = itemView.find(R.id.reorder)
        var eq: ImageView = itemView.find(R.id.equalizer_view)
        var option: AppCompatImageView
        var mListItem: RelativeLayout? = null

        //intialization of our recycler ui elements
        init {
            eq.visibility = View.INVISIBLE
            option = itemView.find(R.id.item_button_action)
            mListItem = itemView.find(R.id.song_list_item)
        }



    }

}