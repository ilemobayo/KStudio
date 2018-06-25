package com.musicplayer.aow.ui.main.library.activities.artistsonglist

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.ChangePlaystate
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.event.PlaySongEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.utils.TimeUtils.formatDuration
import org.jetbrains.anko.find
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.util.*

/**
 * Created by Arca on 12/2/2017.
 */
class ArtistSongsListAdapter(var context: Context, song: PlayList?, private var activity: Activity): RecyclerView.Adapter<ArtistSongsListAdapter.SongListViewHolder>() {

    private var view:View? = null
    private var mSubscriptions: CompositeSubscription? = null
    val TAG = "SongListAdapter"
    private var songPlayList = song
    private var mSongModel:ArrayList<Song>? = song?.songs as ArrayList<Song>

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: SongListViewHolder, position: Int) {
        val model = mSongModel?.get(position)
        val songName = model?.title
        val songDuration = formatDuration(model?.duration!!)
        val songArtist = model.artist
        holder!!.songTV.text = songName
        val tPosition = position
        holder!!.sPosition.text = tPosition.plus(1).toString()
        holder.songArtist.text = songArtist

        if (model.albumArt != null || model.albumArt != "null") {
            val albumArt = Drawable.createFromPath(model.albumArt)
            if (albumArt != null) {
                holder.albumArt.setImageDrawable(albumArt)
            }else{
                //Drawable Text
                val generator = ColorGenerator.MATERIAL // or use DEFAULT
                // generate random color
                val color1 = generator.randomColor
                val icon = TextDrawable.builder().beginConfig()
                        .width(55)  // width in px
                        .height(55) // height in px
                        .endConfig().buildRect(model.title!!.substring(0,1), color1)
                holder.albumArt.setImageDrawable(icon)
            }
        }else{
            //Drawable Text
            val generator = ColorGenerator.MATERIAL // or use DEFAULT
            // generate random color
            val color1 = generator.randomColor
            val icon = TextDrawable.builder().buildRect(model.title!!.substring(0,1), color1)
            holder.albumArt.setImageDrawable(icon)
        }

        //implementation of item click
        holder.mListItem.setOnClickListener {
            RxBus.instance!!.post(PlaySongEvent(mSongModel?.get(position)!!))
            //holder!!.eq.visibility = View.VISIBLE
        }

        if (Player.instance!!.isPlaying) {
            if (Player.instance!!.playingSong!!.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                holder.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
                holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
            } else {
                holder.songTV.setTextColor(context.resources.getColor(R.color.black))
                holder.songArtist.setTextColor(context.resources.getColor(R.color.black))
            }
        }

        broadcastChange(holder, model)

        //here we set item click for songs
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
                play.setOnClickListener {
                    //Update UI
                    RxBus.instance!!.post(PlayListNowEvent(PlayList(mSongModel), position))
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
                artist.visibility = View.GONE
                //Add to Playlist Operation
                playlist.setOnClickListener {
                    mBottomSheetDialog.dismiss()
                    //Dialog with ListView
                    val context = view!!.context
                    val mSelectPlaylistDialog = BottomSheetDialog(context)
                    val sheetView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog_select_playlist, null)
                    val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                    SoftCodeAdapter().addSongToPlaylist(activity,context, mylist, mSelectPlaylistDialog, model)

                    mSelectPlaylistDialog.setContentView(sheetView)
                    mSelectPlaylistDialog.show()
                    mSelectPlaylistDialog.setOnDismissListener {}
                }
                //Delete Operation
                delete.setOnClickListener{
                    val file = File(model.path)
                    if (file.exists()){
                        if (file.delete()) {
                            var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                            mFilePath += model.path
                            val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                            context.contentResolver.delete( rootUri,
                                    MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
                            removeAt(position, model)
                        }
                    }else{
                        var mFilePath = Environment.getExternalStorageDirectory().absolutePath
                        mFilePath += model.path
                        val rootUri = MediaStore.Audio.Media.getContentUriForPath(mFilePath)
                        context.contentResolver.delete( rootUri,
                                MediaStore.Audio.Media.DATA + "=?", arrayOf( model.path ) )
                        removeAt(position, model)
                    }
                    mBottomSheetDialog.dismiss()
                }
            }
        }

    }

    fun swapCursor(playList: ArrayList<Song>?): ArrayList<Song>? {
        if (mSongModel === playList) {
            return null
        }
        val oldCursor = mSongModel
        this.mSongModel = playList
        if (playList != null) {
            this.notifyDataSetChanged()
        }
        return oldCursor
    }

    private fun removeAt(position: Int, song: Song) {
        mSongModel?.remove(song)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mSongModel?.size!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_local_music, parent, false)
        return SongListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel?.size!!
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
                                        if (Player.instance!!.playingSong!!.path!!.toLowerCase().equals(model.path!!.toLowerCase())) {
                                            holder!!.songTV.setTextColor(context.resources.getColor(R.color.red_dim))
                                            holder.songArtist.setTextColor(context.resources.getColor(R.color.red_dim))
                                        } else {
                                            holder!!.songTV.setTextColor(context.resources.getColor(R.color.black))
                                            holder.songArtist.setTextColor(context.resources.getColor(R.color.black))
                                        }
                                    }
                                }
                            }
                        })?.subscribe(RxBus.defaultSubscriber())!!
        )
    }

    class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var songTV: TextView = itemView.find(R.id.text_view_name)
        var sPosition: TextView = itemView.find(R.id.text_view_duration)
        var songArtist: TextView = itemView.find(R.id.text_view_artist)
        var albumArt: ImageView = itemView.find(R.id.image_view_file)
        var eq: ImageView = itemView.find(R.id.equalizer_view)
        var option: AppCompatImageView = itemView.find(R.id.item_button_action)
        var mListItem: RelativeLayout = itemView.find(R.id.song_list_item)
    }
}