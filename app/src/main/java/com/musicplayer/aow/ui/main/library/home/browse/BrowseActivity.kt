package com.musicplayer.aow.ui.main.library.home.browse

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist.PlayListFavDatabase
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.song.SongFavDatabase
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceHolderSearchData
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.browse_list_activity.*
import org.jetbrains.anko.find
import org.json.JSONArray
import org.json.JSONException
import com.musicplayer.aow.delegates.data.db.AppExecutors


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
                val host = intent.getStringExtra("host")
                if(host != null) {
                    val placeholder = gson.fromJson(data, PlaceHolderSearchData::class.java)
                    toolbar.title = placeholder.name
                    item_name.text = placeholder.name
                    viewModelBrowser(null, placeholder, tracks)
                }else {
                    val placeholder = gson.fromJson(data, PlaceholderData::class.java)
                    toolbar.title = placeholder.name
                    item_name.text = placeholder.name
                    viewModelBrowser(placeholder, null, tracks)
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
                    delete.visibility = View.GONE
                    val album = sheetView.find<LinearLayout>(R.id.menu_item_go_to_album)
                    album.visibility = View.GONE
                    val artist = sheetView.find<LinearLayout>(R.id.menu_item_go_to_artist)
                    artist.visibility = View.GONE
                    val playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
                    play.setOnClickListener {
                        //Update UI
                        Player.instance?.play(PlayList(tracks.tracks), 0)
                        mBottomSheetDialog.dismiss()
                    }
                    //play next
                    playNext.setOnClickListener {
                        val playingIndex = Player.instance!!.mPlayList!!.playingIndex
                        Player.instance!!.insertnext(playingIndex,tracks.tracks!!)
                        mBottomSheetDialog.dismiss()
                    }
                    //add to now playing
                    addToQueue.setOnClickListener {
                        Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,tracks.tracks!!)
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

                        SoftCodeAdapter().addSongToPlaylist(context, mylist, mSelectPlaylistDialog, Track(), tracks.tracks, true)

                        mSelectPlaylistDialog.setContentView(sheetView)
                        mSelectPlaylistDialog.show()
                        mSelectPlaylistDialog.setOnDismissListener {}
                    }
                    //Delete Operation
                    delete.setOnClickListener {

                        mBottomSheetDialog.dismiss()
                    }
                }


                Glide.with(this)
                        .load(tracks.picture)
                        .apply(
                                RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .dontAnimate()
                                        .dontTransform()
                        )
                        .into(album_art)

                recycler_views.layoutManager = LinearLayoutManager(this)
                recycler_views.addItemDecoration(DividerItemDecoration(this.getDrawable(R.drawable.drawble_divider),false, false))
                recycler_views.adapter = BrowseAdapter(applicationContext, tracks, this )
            }
        }

    }

    fun favorite(playList: PlayList, track: Track, isPlaylist: Boolean){
        if(isPlaylist) {
            AppExecutors.instance?.diskIO()?.execute {
                val fSong = playListFavDatabase?.playlistFavDAO()?.fetchOnePlayListMxpId(playList.mxp_id!!)
                fSong?.observe(this, object : Observer<PlayList> {
                    override fun onChanged(t: PlayList?) {
                        var fav: Boolean
                        fav = t != null
                        item_button_option_like.setImageResource(if (fav) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
                        item_button_option_like.setOnClickListener {
                            //save to favorite
                            if (fav) {
                                deleteFromFavorite(playList, track, isPlaylist)
                            } else {
                                addToFavorite(playList, track, isPlaylist)
                            }
                        }
                    }
                })
            }
        }else{
            AppExecutors.instance?.diskIO()?.execute {
                val fSong = songFavDatabase?.songFavDAO()?.fetchOneSongPath(track.path!!)
                fSong?.observe(this, Observer<Track> { t ->
                    var fav = true
                    fav = t != null
                    item_button_option_like.setImageResource(if (fav) R.drawable.ic_favorite_yes else R.drawable.ic_favorite_no)
                    item_button_option_like.setOnClickListener {
                        //save to favorite
                        if (fav) {
                            deleteFromFavorite(playList, track, isPlaylist)
                        } else {
                            addToFavorite(playList, track, isPlaylist)
                        }
                    }
                })
            }
        }

    }

    private fun addToFavorite(playList: PlayList = PlayList(), track: Track = Track(), isPlaylist: Boolean = true){
        if(isPlaylist) {
            AppExecutors.instance?.diskIO()?.execute {
                playList.currentTrack?.value = Track()
                playListFavDatabase?.playlistFavDAO()?.insertOnePlayList(playList)
            }
        }else{
            AppExecutors.instance?.diskIO()?.execute { songFavDatabase?.songFavDAO()?.insertOneSong(track) }
        }
    }

    private fun deleteFromFavorite(playList: PlayList = PlayList(), track: Track = Track(), isPlaylist: Boolean = true){
        if(isPlaylist) {
            AppExecutors.instance?.diskIO()?.execute {
                playListFavDatabase?.playlistFavDAO()?.deletePlayList(playList)
            }
        }else{
            AppExecutors.instance?.diskIO()?.execute{ songFavDatabase?.songFavDAO()?.deleteSong(track) }
        }
    }

    private fun viewModelBrowser(data1: PlaceholderData? = null, data2: PlaceHolderSearchData? = null, tracks: PlayList){
        if(data1 != null && data2 == null){
            val placeholder = data1
            if (placeholder.type.equals("playlist", true)  || placeholder.type.equals("album", true)){
                item_owner.text = placeholder.owner
                item_element_list.text = placeholder.description
                item_element_list.setOnClickListener{
                    val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
                    dialog.setTitle("Description")
                    dialog.setMessage(placeholder.description)
                    dialog.setNegativeButton("Close",
                            {dialog, which ->

                            }).create()
                    dialog.show()
                }
                tracks.name = placeholder.name
                tracks.picture = placeholder.picture
                tracks.mxp_id = placeholder._id
                placeholder.member.forEach {
                    val song = Track(it.name,
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
                    if(tracks.picture == null) {
                        tracks.picture = it.picture
                    }
                    tracks.tracks?.add(song)
                }
                //set favorite
                favorite(tracks, Track(), true)
            }else{
                item_owner.text = placeholder.owner
                item_element_list.text = placeholder.description
                item_element_list.setOnClickListener{
                    val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
                    dialog.setTitle("Description")
                    dialog.setMessage(placeholder.description)
                    dialog.setNegativeButton("Close",
                            {dialog, which ->

                            }).create()
                    dialog.show()
                }
                tracks.name = placeholder.name
                tracks.picture = placeholder.picture
                tracks.mxp_id = placeholder._id
                val song = Track(placeholder.name,
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
                tracks.tracks?.add(song)

                //set favorite
                favorite(PlayList(), song, false)
            }
        }else if(data1 == null && data2 != null){
            val placeholder = data2
            if (placeholder.type.equals("playlist", true)  || placeholder.type.equals("album", true)){
                item_owner.text = placeholder.owner
                item_element_list.text = placeholder.description
                item_element_list.setOnClickListener{
                    val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
                    dialog.setTitle("Description")
                    dialog.setMessage(placeholder.description)
                    dialog.setNegativeButton("Close",
                            {dialog, which ->

                            }).create()
                    dialog.show()
                }
                tracks.name = placeholder.name
                tracks.picture = placeholder.picture
                tracks.mxp_id = placeholder._id
                val mItem = JSONArray(placeholder.member)
                for (i in 0..(mItem.length().minus(1))) {
                    try {
                        val item = mItem.getJSONObject(i)
                        val memberItem = item.getJSONArray("member")
                        for (x in 0..(memberItem?.length()!!.minus(1))) {
                            val members = memberItem.getJSONObject(x)
                            val mMemberItem = PlaceholderData()
                            mMemberItem._id = members.getString("_id")
                            mMemberItem.name = members.getString("name")
                            mMemberItem.type = members.getString("type")
                            mMemberItem.owner = members.getString("owner")
                            mMemberItem.picture = members.getString("picture")
                            if (mMemberItem.picture == "" || mMemberItem.picture == null){
                                mMemberItem.picture = "http://zuezhome.com/play/ic_logo.png"
                            }
                            mMemberItem.location = members.getString("location")
                            mMemberItem.description = members.getString("description")
                            mMemberItem.dateCreated = members.getString("date_created")

                            val song = Track(
                                    mMemberItem.name,
                                    mMemberItem.name,
                                    mMemberItem.owner,
                                    "name",
                                    mMemberItem.location,
                                    130000,
                                    1000,
                                    false,
                                    0,
                                    "",
                                    mMemberItem.picture)
                            song.mxp_id = mMemberItem._id
                            tracks.tracks?.add(song)
                        }
                    }catch (e: JSONException){

                    }
                }

                //set favorite
                favorite(tracks, Track(), true)
            }else{
                item_owner.text = placeholder.owner
                item_element_list.text = placeholder.description
                item_element_list.setOnClickListener{
                    val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
                    dialog.setTitle("Description")
                    dialog.setMessage(placeholder.description)
                    dialog.setNegativeButton("Close",
                            {dialog, which ->

                            }).create()
                    dialog.show()
                }
                tracks.name = placeholder.name
                tracks.picture = placeholder.picture
                tracks.mxp_id = placeholder._id
                val song = Track(placeholder.name,
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
                tracks.tracks?.add(song)

                //set favorite
                favorite(PlayList(), song, false)
            }
        }
    }

}