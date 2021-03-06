package com.musicplayer.aow.ui.main.library.playlist.online

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.adapters.onlinefavorites.playlist.PlayListFavDatabase
import com.musicplayer.aow.ui.main.library.playlist.online.playlistsongs.PlaylistSongsActivity
import org.jetbrains.anko.find
import java.util.*

class OnlinePlaylistAdapter(var context: Context, data: ArrayList<PlayList>?, inflater: LayoutInflater): RecyclerView.Adapter<OnlinePlaylistAdapter.OnlinePlayListViewHolder>() {

    val TAG = this.javaClass.name
    private var mModel = data
    private var view: View? = null
    private var layoutInflater = inflater
    private var playListFavDatabase = PlayListFavDatabase.getsInstance(Injection.provideContext()!!)

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: OnlinePlayListViewHolder, position: Int) {
        val model = mModel?.get(position)
        val playlistName = model?.name
        holder.pName.text = playlistName

        val gsonBuilder = GsonBuilder().create()
        val jsonFromPojo = gsonBuilder.toJson(model)
        val intent = Intent(context, PlaylistSongsActivity::class.java)
        intent.putExtra("data", jsonFromPojo)
        intent.putExtra("name", model?.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //implementation of item click
        holder.item?.setOnClickListener {
            ContextCompat.startActivity(context, intent, null)
        }

        //here we set item click for tracks
        //to set options
        holder.pOption.setOnClickListener {
            if (view != null) {
                val context = view!!.context
                val mBottomSheetDialog = BottomSheetDialog(context)
                val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_playlist, null)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()

                val play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
                val playNext = sheetView.find<LinearLayout>(R.id.menu_item_play_next)
                val queue = sheetView.find<LinearLayout>(R.id.menu_item_add_to_queue)
                val delete = sheetView.find<LinearLayout>(R.id.menu_item_delete)
                val rename = sheetView.find<LinearLayout>(R.id.menu_item_rename)
                val clear = sheetView.find<LinearLayout>(R.id.menu_item_clear)
                val delete_label = sheetView.find<TextView>(R.id.delete)
                delete_label.text = context.getString(R.string.remove)
                clear.visibility = View.GONE
                rename.visibility = View.GONE

                //Don't show the delete and rename button for default playlists
                if(playlistName == context.getString(R.string.mp_play_list_tracks) ||
                        playlistName == context.getString(R.string.mp_play_list_nowplaying) ||
                        playlistName == context.getString(R.string.mp_play_list_favorite) ){
                    rename.visibility = View.GONE
                    delete.visibility = View.GONE
                }

                if(playlistName == context.getString(R.string.mp_play_list_tracks)){
                    clear.visibility = View.GONE
                }

                play.setOnClickListener {
                    Player.instance?.play(model!!,0)
                    mBottomSheetDialog.dismiss()
                }
                playNext.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.playingIndex,model?.tracks as ArrayList<Track>)
                    mBottomSheetDialog.dismiss()
                }
                queue.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,model?.tracks as ArrayList<Track>)
                    mBottomSheetDialog.dismiss()
                }
                delete.setOnClickListener{
                    AppExecutors.instance?.diskIO()?.execute {
                        playListFavDatabase?.playlistFavDAO()?.deletePlayList(model!!)
                    }
                    mBottomSheetDialog.dismiss()
                }
            }
        }
    }


    fun swapCursor(artistList: ArrayList<PlayList>?): ArrayList<PlayList>? {
        if (mModel === artistList) {
            return null
        }
        val oldCursor = mModel
        this.mModel = artistList
        if (artistList != null) {
            this.notifyDataSetChanged()
        }
        return oldCursor
    }

    private fun showPlaylistRenameDialog(playList: PlayList) {
        val dialogBuilder = AlertDialog.Builder(view!!.context, android.R.style.Theme_Material_Light_Dialog)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_input, null)
        dialogBuilder.setView(dialogView)

        val edt = dialogView.find<EditText>(R.id.edit1)
        edt.setText(playList.name)

        dialogBuilder.setTitle("Rename \""+ playList.name +"\" to").setIcon(R.drawable.ic_play_now_rename)
        dialogBuilder.setPositiveButton("Save", { dialog, whichButton ->
            //do something with edt.getText().toString();
            val newName = edt.text.toString()
            if (true) {
                renamePlayList(playList._id, newName)
            }
        })
        dialogBuilder.setNegativeButton("Cancel", { dialog, whichButton ->
            //pass
        })
        val b = dialogBuilder.create()
        b.show()
    }

    private fun renamePlayList(playListId: Long, newName:String) {
        val values = ContentValues()
        values.put(MediaStore.Audio.Playlists.NAME, newName)
        context.contentResolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, "_id=$playListId", null)

    }

    private fun deletePlayList(id: Long) {
        try {
            val uri_ = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
            val whereclause = MediaStore.Audio.Playlists._ID + " =?"
            context.contentResolver.delete(uri_, whereclause, arrayOf(id.toString()))
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlinePlayListViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_play_list,parent,false)
        return OnlinePlayListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mModel?.size!!
    }

    class OnlinePlayListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var pName: TextView = itemView.find(R.id.text_view_name)
        var pOption: AppCompatImageView = itemView.find(R.id.image_button_action)
        var item: RelativeLayout? = itemView.find(R.id.item)
    }

}