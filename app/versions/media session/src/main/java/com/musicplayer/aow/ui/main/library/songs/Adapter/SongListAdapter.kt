package com.musicplayer.aow.ui.main.library.songs.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.l4digital.fastscroll.FastScroller
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.model.TempSongs
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.ChangePlaystate
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.event.ReloadEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.main.library.activities.AlbumSongs
import com.musicplayer.aow.ui.main.library.activities.ArtistSongs
import com.musicplayer.aow.ui.main.library.songs.dialog.adapter.PlaylistDialogAdapter
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.TimeUtils.formatDuration
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.util.*


/**
 * Created by Arca on 11/9/2017.
 */
class SongListAdapter(
        context: Context,
        song: PlayList,
        activity: FragmentActivity) : RecyclerView.Adapter<SongListAdapter.SongListViewHolder>()
        , View.OnClickListener, FastScroller.SectionIndexer{

    private var activity = activity
    private var view:View? = null
    private var mSubscriptions: CompositeSubscription? = null
    val TAG = "SongListAdapter"
    var context = context.applicationContext
    private var songPlayList = song
    private val mSongModel = song.songs as ArrayList<Song>

    override fun onClick(v: View?) {

    }

    override fun getSectionText(position: Int): String {
        return mSongModel[position].title!!.first().toString()
    }

    override fun onBindViewHolder(holder: SongListViewHolder?, position: Int) {
        var model = mSongModel[position]
        var songArtist = model.artist
        holder!!.songTV.text = model.title
        holder.duration.text = formatDuration(model.duration)
        holder.songArtist.text = "$songArtist" +  " - " + model.album

        if (Player.instance!!.isPlaying) {
            if (Player.instance!!.playingSong!!.title!!.toLowerCase().equals(model.title!!.toLowerCase())) {
                holder!!.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
                holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
            } else {
                holder!!.songTV.setTextColor(context.resources.getColor(R.color.black))
                holder.songArtist.setTextColor(context.resources.getColor(R.color.black))
            }
        }else if(position == 0){
            holder!!.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
            holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
        }

        broadcastChange(holder, model)

        var icon = TextDrawable.builder().beginConfig()
                .textColor(Color.WHITE)
                .width(55)  // width in px
                .height(55) // height in px
                .endConfig().buildRect(model.title!!.substring(0,1), context.resources.getColor(R.color.blue))
        if (model.albumArt != null || model.albumArt != "null") {
            doAsync {
                val albumArt = Drawable.createFromPath(model.albumArt)
                onComplete {
                    if (albumArt != null) {
                        holder.albumArt.setImageDrawable(albumArt)
                    } else {
                        holder.albumArt.setImageDrawable(icon)
                    }
                }
            }
        }else{
            holder.albumArt.setImageDrawable(icon)
        }

        //implementation of item click
        holder.mListItem.setOnClickListener {
            RxBus.instance!!.post(PlayListNowEvent(songPlayList, position))
            //notifyItemRangeChanged(position, mSongModel.size)
        }

        //here we set item click for songs
        //to set options
        holder.option.setOnClickListener {
            if (view != null) {
                var context = view!!.context
                val mBottomSheetDialog = BottomSheetDialog(context)
                val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_all_music, null)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()
                mBottomSheetDialog.setOnDismissListener {
                    //perform action on close
                }

                var play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
                var playNext = sheetView.find<LinearLayout>(R.id.menu_item_play_next)
                var addToQueue = sheetView.find<LinearLayout>(R.id.menu_item_add_to_queue)
                var delete = sheetView.find<LinearLayout>(R.id.menu_item_delete)
                var album = sheetView.find<LinearLayout>(R.id.menu_item_go_to_album)
                var artist = sheetView.find<LinearLayout>(R.id.menu_item_go_to_artist)
                var playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
                play.setOnClickListener {
                    //Update UI
                    RxBus.instance!!.post(PlayListNowEvent(PlayList(mSongModel), position))
                    mBottomSheetDialog.dismiss()
                }
                //play next
                playNext.setOnClickListener {
                    var playingIndex = Player.instance!!.mPlayList!!.playingIndex
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
                    val intent = Intent(context, ArtistSongs::class.java)
                    intent.putExtra("com.musicplayer.aow.artist.name", model.artist)
                    ContextCompat.startActivity(context, intent, null)
                    mBottomSheetDialog.dismiss()
                }
                //Add to Playlist Operation
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
                                        var playListAdapter = PlaylistDialogAdapter(activity, playList, model, mSelectPlaylistDialog)
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
                //Delete Operation
                delete.setOnClickListener{
                    var file = File(model.path)
                    if (file.exists()){
                        if (file.delete()) {
                            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                            mFilePath += model.path
                            val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                            context.contentResolver.delete( rootUri,
                                    MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
                            RxBus.instance!!.post(ReloadEvent(model))
                        }
                    }else{
                        var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                        mFilePath += model.path
                        val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                        context.contentResolver.delete( rootUri,
                                MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
                        RxBus.instance!!.post(ReloadEvent(model))
                    }
                    mBottomSheetDialog.dismiss()
                }
            }
        }
    }

    private fun removeAt(position: Int, song: Song) {
        mSongModel.remove(song)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mSongModel.size)
        refreshDatabase(song)
    }

    private fun refreshDatabase(song: Song){
        doAsync {
            TempSongs.instance!!.setSongs()
            onComplete {
                updateAllSongsPlayList(PlayList(TempSongs.instance!!.songs), song)
            }
        }
    }

    private fun updateAllSongsPlayList(playList: PlayList, song: Song) {
        val subscription = AppRepository().setInitAllSongs(playList).subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                    }
                })
        mSubscriptions?.add(subscription)
    }

    private fun broadcastChange(holder: SongListViewHolder?, model: Song){
        if (mSubscriptions == null) {
            mSubscriptions = CompositeSubscription()
        }
        mSubscriptions!!.add(
                RxBus.instance?.toObservable()
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.doOnNext({ o ->
                            if (o is ChangePlaystate) {
                                if (o != false) {
                                    if (Player.instance!!.isPlaying) {
                                        if (Player.instance!!.playingSong!!.title!!.toLowerCase().equals(model.title!!.toLowerCase())) {
                                            holder!!.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
                                            holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
                                        } else {
                                            holder!!.songTV.setTextColor(context.resources.getColor(R.color.black))
                                            holder.songArtist.setTextColor(context.resources.getColor(R.color.black))
                                        }
                                    }
                                }
                            }
                            if (o is ReloadEvent) {
                                if (o != null) {
                                    mSongModel.forEachIndexed { index, song ->
                                        if (song.displayName!!.toLowerCase().equals(o.song!!.displayName!!.toLowerCase()) &&
                                                song.title!!.toLowerCase().equals(o.song!!.title!!.toLowerCase())){
                                            removeAt(index, song)
                                        }
                                    }
                                }
                            }
                        })?.subscribe(RxBus.defaultSubscriber())!!
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongListViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_local_music, parent, false)
        return SongListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
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


