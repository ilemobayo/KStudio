package com.musicplayer.aow.ui.main.library.playlist.online.playlistsongs

import android.app.Activity
import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.utils.TimeUtils
import org.jetbrains.anko.find

class PlaylistSongAdapter(var context: Context, song: PlayList, private var activity: Activity): RecyclerView.Adapter<PlaylistSongAdapter.ListViewHolder>() {

    private var view: View? = null
    val TAG = "PlaylistListAdapter"
    private var songPlayList = song
    val mSongModel = song.tracks as ArrayList<Track>

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val model = mSongModel[position]
        val songName = model.title
        val songDuration = TimeUtils.formatDuration(model.duration)
        val songArtist = model.artist
        val tPosition = position
        holder.duration.text = tPosition.plus(1).toString()
        holder.songTV.text = songName
        holder.songArtist.text = songArtist

        //implementation of item click
        holder.mListItem.setOnClickListener {
            Player.instance?.play(mSongModel[position])
            //holder!!.eq.visibility = View.VISIBLE
        }

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
                val album = sheetView.find<LinearLayout>(R.id.menu_item_go_to_album)
                val artist = sheetView.find<LinearLayout>(R.id.menu_item_go_to_artist)
                val playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
                var delete_label = sheetView.find<TextView>(R.id.delete_label)
                album.visibility = View.GONE
                artist.visibility = View.GONE
                delete.visibility = View.GONE
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
                //add to now playing
                addToQueue.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,model)
                    mBottomSheetDialog.dismiss()
                }
                //Add to Playlist Operation
                playlist.setOnClickListener {
                    mBottomSheetDialog.dismiss()
                    //Dialog with ListView
                    val context = view!!.context
                    val mSelectPlaylistDialog = BottomSheetDialog(context)
                    val sheetView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog_select_playlist, null)
                    val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                    SoftCodeAdapter().addSongToPlaylist(context, mylist, mSelectPlaylistDialog, model)

                    mSelectPlaylistDialog.setContentView(sheetView)
                    mSelectPlaylistDialog.show()
                    mSelectPlaylistDialog.setOnDismissListener {}
                }
            }
        }

    }

    private fun removeAt(position: Int, track: Track) {
        mSongModel.remove(track)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mSongModel.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_music, parent, false)
        return ListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
    }

    private fun broadcastChange(holder: ListViewHolder?, model: Track){
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

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var songTV: TextView = itemView.find(R.id.text_view_name)
        var duration: TextView = itemView.find(R.id.text_view_duration)
        var songArtist: TextView = itemView.find(R.id.text_view_artist)
        var albumArt: ImageView = itemView.find(R.id.image_view_file)
        var eq: ImageView = itemView.find(R.id.equalizer_view)
        var option: AppCompatImageView = itemView.find(R.id.item_button_action)
        var mListItem: RelativeLayout = itemView.find(R.id.song_list_item)
    }
}