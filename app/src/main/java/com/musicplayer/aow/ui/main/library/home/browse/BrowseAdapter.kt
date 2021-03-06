package com.musicplayer.aow.ui.main.library.home.browse

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.l4digital.fastscroll.FastScroller
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.exo.AudioDownloadService
import com.musicplayer.aow.delegates.exo.DownloadUtil
import com.musicplayer.aow.delegates.player.IPlayback
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase
import com.musicplayer.aow.ui.main.library.activities.AlbumSongs
import com.musicplayer.aow.ui.main.library.home.artist.ArtistOnline
import org.jetbrains.anko.find
import java.io.File


/**
 * Created by Arca on 11/9/2017.
 */
class BrowseAdapter(
        var context: Context,
        song: PlayList,
        private var activity: FragmentActivity
) : RecyclerView.Adapter<BrowseAdapter.TrackViewHolder>(),
        FastScroller.SectionIndexer{

    val TAG = this.javaClass.name
    private var view:View? = null
    private var songFavDatabase: SongFavDatabase? = SongFavDatabase.getsInstance(context.applicationContext)
    private var mTrackModel: ArrayList<Track>? = song.tracks as ArrayList<Track>
    private var mPlayer = Player.instance
    private var callback: IPlayback.Callback? = null

    override fun getSectionText(position: Int): String {
        return mTrackModel?.get(position)?.title!!.first().toString()
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val model = mTrackModel?.get(position)
        if (model != null) {
            if (Player.instance?.mPlayList != null) {
                if (Player.instance?.mPlayList?.currentTrack?.value?.path?.toLowerCase().equals(model.path?.toLowerCase())) {
                    holder.position.visibility = View.INVISIBLE
                    holder.button_play_toggle.visibility = View.VISIBLE
                    holder.loading.visibility = View.INVISIBLE
                } else {
                    holder.position.visibility = View.VISIBLE
                    holder.button_play_toggle.visibility = View.INVISIBLE
                    holder.loading.visibility = View.INVISIBLE
                }
            }

            AppExecutors.instance?.diskIO()?.execute {
                val fSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(model.path!!)
                fSong?.observe(this.activity, object : Observer<Track> {
                    override fun onChanged(t: Track?) {
                        var fav = true
                        fav = t != null
                        holder.favorite.setImageResource(if (fav) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
                        holder.favorite.setOnClickListener {
                            //save to favorite
                            if (fav) {
                                deleteFromFavorite(model)
                            } else {
                                addToFavorite(model)
                            }
                        }
                    }
                })
            }

            //implementation of item click
            holder.mListItem.setOnClickListener {
                Player.instance?.play(PlayList(mTrackModel), position)
            }
            callback = object : IPlayback.Callback {
                override fun onSwitchLast(last: Track?) {
                    if (Player.instance!!.mPlayList != null) {
                        if (Player.instance!!.mPlayList!!.currentTrack!!.value?.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                            holder.position.visibility = View.INVISIBLE
                            holder.button_play_toggle.visibility = View.VISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        } else {
                            holder.position.visibility = View.VISIBLE
                            holder.button_play_toggle.visibility = View.INVISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        }
                    }
                }
                override fun onSwitchNext(next: Track?) {
                    if (Player.instance!!.mPlayList != null) {
                        if (Player.instance!!.mPlayList!!.currentTrack!!.value?.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                            holder.position.visibility = View.INVISIBLE
                            holder.button_play_toggle.visibility = View.VISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        } else {
                            holder.position.visibility = View.VISIBLE
                            holder.button_play_toggle.visibility = View.INVISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        }
                    }
                }
                override fun onComplete(next: Track?) {
                    holder.position.visibility = View.VISIBLE
                    holder.button_play_toggle.visibility = View.INVISIBLE
                    holder.loading.visibility = View.INVISIBLE
                    if (Player.instance!!.mPlayList != null) {
                        if (Player.instance!!.mPlayList!!.currentTrack!!.value?.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                            holder.position.visibility = View.INVISIBLE
                            holder.button_play_toggle.visibility = View.VISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        } else {
                            holder.position.visibility = View.VISIBLE
                            holder.button_play_toggle.visibility = View.INVISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        }
                    }
                }
                override fun onPlayStatusChanged(isPlaying: Boolean) {
                    if (Player.instance?.mPlayList != null) {
                        if (Player.instance?.mPlayList?.currentTrack?.value?.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                            holder.position.visibility = View.INVISIBLE
                            holder.button_play_toggle.visibility = View.VISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        } else {
                            holder.position.visibility = View.VISIBLE
                            holder.button_play_toggle.visibility = View.INVISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        }
                    }
                }
                override fun onTriggerLoading(isLoading: Boolean) {
                    if (Player.instance?.mPlayList != null) {
                        if (Player.instance?.mPlayList?.currentTrack?.value?.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                            holder.position.visibility = View.INVISIBLE
                            holder.button_play_toggle.visibility = View.INVISIBLE
                            holder.loading.visibility = View.VISIBLE
                        } else {
                            holder.position.visibility = View.VISIBLE
                            holder.button_play_toggle.visibility = View.INVISIBLE
                            holder.loading.visibility = View.INVISIBLE
                        }
                    }
                }

                override fun onPrepared(isPrepared: Boolean) {
                    
                }
            }
            mPlayer?.registerCallback(callback!!)
            loadViews(model, holder, position)
        }
    }

    fun loadViews(model: Track, holder: TrackViewHolder?, position: Int){
        val songArtist = model.artist
        holder!!.songTV.text = model.title
        holder.songArtist.text = "$songArtist"
        val tPosition = position
        holder.position.text = tPosition.plus(1).toString()

        // create the data spec of a given media file
        val uri = Uri.parse(model.path)
        val dataSpec = DataSpec(uri)
        val cache = DownloadUtil.getCache(context)
        // get information about what is cached for the given data spec
        val counters = CacheUtil.CachingCounters();
        CacheUtil.getCached(dataSpec, cache, counters);
        if (counters.contentLength == counters.totalCachedBytes()) {
            // all bytes cached
            holder.position.setTextColor(Color.GREEN)
        } else if (counters.totalCachedBytes().toInt() == 0){
            // not cached at all
        } else {
            // partially cached
            holder.position.setTextColor(Color.RED)
        }

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
                val _delete = sheetView.findViewById<ImageView>(R.id.delete_img)
                _delete.setImageResource(R.drawable.ic_file_download)
                val _delete_label = sheetView.find<TextView>(R.id.delete_label)
                _delete_label.text = context.getString(R.string.make_available_offline)
                delete.visibility = View.GONE
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
                    val intent = Intent(context, AlbumSongs::class.java)
                    intent.putExtra("com.musicplayer.aow.album.name", model.album)
                    ContextCompat.startActivity(context, intent, null)
                    mBottomSheetDialog.dismiss()
                }
                artist.setOnClickListener {
                    val intent = Intent(context, ArtistOnline::class.java)
                    intent.putExtra("artist", model.artist)
                    intent.putExtra("des", "")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ContextCompat.startActivity(context, intent, null)
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

                //Delete Operation
                delete.setOnClickListener {
                    val action = ProgressiveDownloadAction(
                            Uri.parse(mTrackModel!![position].path),
                            false,
                            null,
                            "")

                    DownloadService.startWithAction(
                            context.applicationContext,
                            AudioDownloadService::class.java,
                            action,
                            true)

                    mBottomSheetDialog.dismiss()
                }
            }
        }
    }

    private fun addToFavorite(track: Track){
        AppExecutors.instance?.diskIO()?.execute{ songFavDatabase?.songFavDAO()?.insertOneSong(track) }
    }

    private fun deleteFromFavorite(track: Track){
        AppExecutors.instance?.diskIO()?.execute{ songFavDatabase?.songFavDAO()?.deleteSong(track) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mTrackModel!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemView = itemView
        var songTV: TextView = itemView.find(R.id.text_view_name)
        var songArtist: TextView = itemView.find(R.id.text_view_artist)
        var position: TextView = itemView.find(R.id.position)
        var loading: SpinKitView = itemView.find(R.id.loading)
        var button_play_toggle: AppCompatImageView = itemView.find(R.id.button_play_toggle)
        var option: AppCompatImageView = itemView.find(R.id.item_button_action)
        var favorite: AppCompatImageView = itemView.find(R.id.item_button_action_like)
        var mListItem: RelativeLayout = itemView.find(R.id.song_list_item)
    }

}


