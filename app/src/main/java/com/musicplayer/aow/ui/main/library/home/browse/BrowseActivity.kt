package com.musicplayer.aow.ui.main.library.home.browse

import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist.PlayListFavDatabase
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.browse_list_activity.*
import org.jetbrains.anko.find


/**
 * Created by Arca on 2/16/2018.
 */
class BrowseActivity: BaseActivity(){

    val TAG = this.javaClass.name
    private var playListFavDatabase = PlayListFavDatabase.getsInstance(Injection.provideContext()!!)
    private var songFavDatabase: SongFavDatabase? = SongFavDatabase.getsInstance(Injection.provideContext()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.browse_list_activity)
        ButterKnife.bind(this)

        val tintManager = SystemBarTintManager(this)
        // enable status bar tint
        tintManager.isStatusBarTintEnabled = true
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true)

        // set a custom tint color for all system bars
        tintManager.setTintColor(R.color.translusent)
        // set a custom navigation bar resource
        tintManager.setNavigationBarTintResource(R.drawable.gradient_warning)
        // set a custom status bar drawable
        tintManager.setStatusBarTintResource(R.color.black)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }


        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("data")
            val tracks: PlayList = PlayList()
            if (data != null) {
                val gson = Gson()
                val placeholder = gson.fromJson(data, PlaceholderData::class.java)
                toolbar.title = placeholder.name
                item_name.text = placeholder.name
                if (placeholder.type.equals("playlist", true)  || placeholder.type.equals("album", true)){
                    item_owner.text = placeholder.owner
                    item_element_list.text = placeholder.description
                    tracks.name = placeholder.name
                    tracks.picture = placeholder.picture
                    tracks.mxp_id = placeholder._id
                    placeholder.member.forEach {
                        val song = Song(it.name,
                                it.name,
                                it.owner,
                                "name",
                                it.location,
                                130000,
                                1000,
                                false,
                                0,
                                "",
                                it.picture)
                        song.mxp_id = it._id
                        tracks.songs?.add(song)
                    }
                    //set favorite
                    favorite(tracks, Song(), true)
                }else{
                    item_owner.text = placeholder.owner
                    item_element_list.text = placeholder.description
                    tracks.name = placeholder.name
                    tracks.picture = placeholder.picture
                    tracks.mxp_id = placeholder._id
                    val song = Song(placeholder.name,
                            placeholder.name,
                            placeholder.owner,
                            "name",
                            placeholder.location,
                            130000,
                            1000,
                            false,
                            0,
                            "",
                            placeholder.picture)
                    song.mxp_id = placeholder._id
                    tracks.songs?.add(song)

                    //set favorite
                    favorite(PlayList(), song, false)
                }

                item_button_option.setOnClickListener {
                    val context = this
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
                    album.visibility = View.GONE
                    val artist = sheetView.find<LinearLayout>(R.id.menu_item_go_to_artist)
                    artist.visibility = View.GONE
                    val playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
                    val _delete = sheetView.findViewById<ImageView>(R.id.delete_img)
                    _delete.setImageResource(R.drawable.ic_file_download)
                    val _delete_label = sheetView.find<TextView>(R.id.delete_label)
                    _delete_label.text = context.getString(R.string.download)
                    play.setOnClickListener {
                        //Update UI
                        RxBus.instance!!.post(PlayListNowEvent(PlayList(tracks.songs), 0))
                        mBottomSheetDialog.dismiss()
                    }
                    //play next
                    playNext.setOnClickListener {
                        val playingIndex = Player.instance!!.mPlayList!!.playingIndex
                        Player.instance!!.insertnext(playingIndex,tracks.songs!!)
                        mBottomSheetDialog.dismiss()
                    }
                    //add to now playing
                    addToQueue.setOnClickListener {
                        Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,tracks.songs!!)
                        mBottomSheetDialog.dismiss()
                    }
                    //Add to Playlist Operation
                    playlist.setOnClickListener {
                        mBottomSheetDialog.dismiss()
                        //Dialog with ListView
                        val context = context
                        val mSelectPlaylistDialog = BottomSheetDialog(context)
                        val sheetView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog_select_playlist, null)
                        val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                        SoftCodeAdapter().addSongToPlaylist(this,context, mylist, mSelectPlaylistDialog, Song(), tracks.songs, true)

                        mSelectPlaylistDialog.setContentView(sheetView)
                        mSelectPlaylistDialog.show()
                        mSelectPlaylistDialog.setOnDismissListener {}
                    }
                    //Delete Operation
                    delete.setOnClickListener {
                        //
                        mBottomSheetDialog.dismiss()
                    }
                }


                Glide.with(this)
                        .load(tracks.picture)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .error(R.drawable.gradient_info)
                        .into(album_art)

                recycler_views.layoutManager = LinearLayoutManager(applicationContext)
                recycler_views.addItemDecoration(DividerItemDecoration(this.getDrawable(R.drawable.drawble_divider),false, false))
                recycler_views.adapter = BrowseAdapter(applicationContext, tracks, this )
            }
        }

    }

    fun favorite(playList: PlayList, song: Song, isPlaylist: Boolean){
        val fav = isFavorite(playList, song, isPlaylist)
        item_button_option_like.setImageResource(if (fav) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
        item_button_option_like.setOnClickListener {
            //save to favorite
            if (fav){
                deleteFromFavorite(playList, song, isPlaylist)
                item_button_option_like.
                        setImageResource(
                                if (isFavorite(playList, song, isPlaylist)) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no
                        )
            } else {
                addToFavorite(playList, song, isPlaylist)
                item_button_option_like.
                        setImageResource(if (isFavorite(playList, song, isPlaylist)) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no
                        )
            }
        }
    }

    fun isFavorite(playList: PlayList = PlayList(), song: Song = Song(), isPlaylist: Boolean = true): Boolean{
        //Log.e(TAG, songFavDatabase?.songFavDAO()?.fetchAllSong()?.size.toString())
        if(isPlaylist) {
            val fSong = playListFavDatabase?.playlistFavDAO()?.fetchOnePlayListMxpId(playList.mxp_id!!)
            return fSong != null
        }else{
            val fSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(song.path!!)
            return fSong != null
        }
    }

    private fun addToFavorite(playList: PlayList = PlayList(), song: Song = Song(), isPlaylist: Boolean = true){
        if(isPlaylist) {
            playList.currentSong = Song()
            playListFavDatabase?.playlistFavDAO()?.insertOnePlayList(playList)
        }else{
            songFavDatabase?.songFavDAO()?.insertOneSong(song)
        }
    }

    private fun deleteFromFavorite(playList: PlayList = PlayList(), song: Song = Song(), isPlaylist: Boolean = true){
        if(isPlaylist) {
            playListFavDatabase?.playlistFavDAO()?.deletePlayList(playList)
        }else{
            songFavDatabase?.songFavDAO()?.deleteSong(song)
        }
    }

}