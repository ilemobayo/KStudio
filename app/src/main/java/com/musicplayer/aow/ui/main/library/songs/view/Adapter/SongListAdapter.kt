package com.musicplayer.aow.ui.main.library.songs.view.Adapter

import android.content.Context
import android.content.Intent
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.l4digital.fastscroll.FastScroller
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.PlaylistDatabase
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.IPlayback
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.ui.main.library.activities.ArtistSongs
import com.musicplayer.aow.ui.main.library.songs.view.dialog.adapter.PlaylistDialogAdapter
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import org.jetbrains.anko.find


/**
 * Created by Arca on 11/9/2017.
 */
class SongListAdapter(
        song: PlayList,
        private var activity: FragmentActivity) : RecyclerView.Adapter<SongListAdapter.SongListViewHolder>()
        , FastScroller.SectionIndexer{

    val TAG = this.javaClass.name
    private var mTrackModel: ArrayList<Track>? = song.tracks as ArrayList<Track>
    private var mPlayer = Player.instance
    private var callback: IPlayback.Callback? = null
    private var playlisDatabase: PlaylistDatabase? = PlaylistDatabase.getsInstance(Injection.provideContext()!!)

    override fun getSectionText(position: Int): String {
        return mTrackModel?.get(position)?.title!!.first().toString()
    }

    override fun onBindViewHolder(holder: SongListViewHolder, position: Int) {
        val model = mTrackModel?.get(position)
        if (model != null) {
            callback = object : IPlayback.Callback {
                override fun onSwitchLast(last: Track?) {
                }
                override fun onSwitchNext(next: Track?) {
                }
                override fun onComplete(next: Track?) {
                }
                override fun onPlayStatusChanged(isPlaying: Boolean) {
                    if (Player.instance!!.isPlaying) {
                        if (Player.instance!!.playingTrack!!.path!!.equals(model.path!!, true)) {
                            holder.songTV.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.red_dim))
                            holder.songArtist.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.red_dim))
                        } else {
                            holder.songTV.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.black))
                            holder.songArtist.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.black))
                        }
                    }
                }
                override fun onTriggerLoading(isLoading: Boolean) {
                }

                override fun onPrepared(isPrepared: Boolean) {

                }
            }
            mPlayer?.registerCallback(callback!!)
            loadViews(model, holder, position)
        }
    }

    private fun loadViews(model: Track, holder: SongListViewHolder?, position: Int){
        holder!!.songTV.text = model.title?.toLowerCase()?.capitalize()
        val tPosition = position
        holder.duration.text = tPosition.plus(1).toString()
        holder.songArtist.text = model.artist?.capitalize()

        if (Player.instance!!.isPlaying) {
            if (Player.instance?.playingTrack?.path?.toLowerCase().equals(model.path?.toLowerCase())) {
                holder.songTV.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.red_dim))
                holder.songArtist.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.red_dim))
            } else {
                holder.songTV.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.black))
                holder.songArtist.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.black))
            }
        }

        //implementation of item click
        holder.mListItem.setOnClickListener {
            Player.instance?.play(PlayList(mTrackModel), position)
        }

        //here we set item click for tracks
        //to set options
        holder.option.setOnClickListener {
            if (it != null) {
                val context = it.context
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
                play.setOnClickListener {
                    //Update UI
                    Player.instance?.play(PlayList(mTrackModel), position)
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
                    //Dialog with ListView
                    val mSelectPlaylistDialog = BottomSheetDialog(context)
                    val sheetView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog_select_playlist, null)
                    val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                    AppExecutors.instance?.diskIO()?.execute{
                        var list: ArrayList<PlayList> = ArrayList()
                        list = playlisDatabase?.playlistDAO()?.fetchAllPlayListWithNoRecentlyPlayedNLD() as ArrayList<PlayList>
                        list.sortedWith(compareBy({ (it.name)!!.toLowerCase() }))
                        val adapter = PlaylistDialogAdapter(list, model, ArrayList(), mSelectPlaylistDialog, false)
                        val layoutManager = PreCachingLayoutManager(it.context)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(it!!.context))
                        mylist.setHasFixedSize(true)
                        activity.runOnUiThread {
                            mylist.layoutManager = layoutManager
                            mylist.adapter = adapter
                        }
                    }

                    //SoftCodeAdapter().addSongToPlaylist(activity,context, mylist, mSelectPlaylistDialog, model)

                    mSelectPlaylistDialog.setContentView(sheetView)
                    mSelectPlaylistDialog.show()
                    mSelectPlaylistDialog.setOnDismissListener {}
                }
                //Delete Operation
                delete.setOnClickListener{
                    SoftCodeAdapter().deleteSongFromPhone(context, model)
                    mBottomSheetDialog.dismiss()
                }
            }
        }
    }

    fun swapCursor(playList: ArrayList<Track>?): ArrayList<Track>? {
        if (mTrackModel === playList) {
            return null
        }
        val oldCursor = mTrackModel
        this.mTrackModel = playList
        if (playList != null) {
            this.notifyDataSetChanged()
        }
        return oldCursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        return SongListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_local_music, parent, false))
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mTrackModel!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemView = itemView
        var songTV: TextView = itemView.find(R.id.text_view_name)
        var duration: TextView = itemView.find(R.id.text_view_duration)
        var songArtist: TextView = itemView.find(R.id.text_view_artist)
        var albumArt: ImageView = itemView.find(R.id.image_view_file)
        var eq: ImageView = itemView.find(R.id.equalizer_view)
        var option: AppCompatImageView = itemView.find(R.id.item_button_action)
        var mListItem: RelativeLayout = itemView.find(R.id.song_list_item)
    }

}


