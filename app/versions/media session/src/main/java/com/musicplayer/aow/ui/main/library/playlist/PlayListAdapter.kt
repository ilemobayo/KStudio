package com.musicplayer.aow.ui.main.library.playlist

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.amulyakhare.textdrawable.TextDrawable
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.PlayListNowEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.main.library.playlist.PlaylistSongs.PlaylistSongsListActivity
import org.jetbrains.anko.find
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 * Created by Arca on 12/6/2017.
 */
class PlayListAdapter(context: Context, playlist: List<PlayList>, inflater: LayoutInflater): RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder>() {

    val TAG = "PlayListAdapter"
    var context = context
    private val mSongModel = playlist
    private val mSubscriptions: CompositeSubscription? = null
    private var view:View? = null
    private var layoutInflater = inflater

    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: PlayListViewHolder?, position: Int) {
        var model = mSongModel[position]
        var playlistName = model.name
        var playlistdetails = model.songs.size
        holder!!.pName.text = playlistName
        holder.pDetails.text = playlistdetails.toString()

        //Drawable Text
        //var generator = ColorGenerator.MATERIAL // or use DEFAULT
        // generate random color
        //var color1 = generator.randomColor
        var icon = TextDrawable.builder().beginConfig()
                    .width(55)  // width in px
                    .height(55) // height in px
                    .endConfig().buildRect(playlistName.substring(0,1), context.resources.getColor(R.color.blue))
        holder.label.setImageDrawable(icon)


        //implementation of item click
        holder.item?.setOnClickListener {
            val intent = PlaylistSongsListActivity.newIntent(context)
            intent.putExtra("name", model.name)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, intent, null)
        }

        //here we set item click for songs
        //to set options
        holder.pOption.setOnClickListener {
            if (view != null) {
                var context = view!!.context
                val mBottomSheetDialog = BottomSheetDialog(context)
                val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_playlist, null)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()

                var play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
                var playNext = sheetView!!.find<LinearLayout>(R.id.menu_item_play_next)
                var queue = sheetView!!.find<LinearLayout>(R.id.menu_item_add_to_queue)
                var delete = sheetView.find<LinearLayout>(R.id.menu_item_delete)
                var rename = sheetView.find<LinearLayout>(R.id.menu_item_rename)
                var clear = sheetView.find<LinearLayout>(R.id.menu_item_clear)

                //Don't show the delete and rename button for default playlists
                if(playlistName == context!!.getString(R.string.mp_play_list_songs) ||
                        playlistName == context.getString(R.string.mp_play_list_nowplaying) ||
                        playlistName == context.getString(R.string.mp_play_list_favorite) ){
                    rename.visibility = View.GONE
                    delete.visibility = View.GONE
                }

                if(playlistName == context.getString(R.string.mp_play_list_songs)){
                    clear.visibility = View.GONE
                }

                play.setOnClickListener {
                    RxBus.instance!!.post(PlayListNowEvent(PlayList(model.songs),0))
                    mBottomSheetDialog.dismiss()
                }
                playNext.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.playingIndex,model.songs as ArrayList<Song>)
                    mBottomSheetDialog.dismiss()
                }
                queue.setOnClickListener {
                    Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,model.songs as ArrayList<Song>)
                    mBottomSheetDialog.dismiss()
                }
                rename.setOnClickListener {
                    showPlaylistRenameDialog(model)
                    mBottomSheetDialog.dismiss()
                }
                clear.setOnClickListener {
                    model.setSongs(ArrayList())
                    updatePlayList(model)
                    mBottomSheetDialog.dismiss()
                }
                delete.setOnClickListener{
                    deletePlayList(model)
                    mBottomSheetDialog.dismiss()
                }
            }
        }
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
            if (edt.text.toString() != null) {
                playList.name = edt.text.toString()
                updatePlayList(playList)
            }
        })
        dialogBuilder.setNegativeButton("Cancel", { dialog, whichButton ->
            //pass
        })
        val b = dialogBuilder.create()
        b.show()
    }

    private fun updatePlayList(playList: PlayList) {
        val subscription = AppRepository().update(playList)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(playList: PlayList) {}
                })
        //Update UI
        RxBus.instance!!.post(PlayListAction(true))
        mSubscriptions?.add(subscription)
    }

    private fun deletePlayList(playList: PlayList) {
        val subscription = AppRepository().delete(playList)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(playList: PlayList) {}
                })
        //Update UI
        RxBus.instance!!.post(PlayListAction(true))
        mSubscriptions?.add(subscription)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PlayListViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_play_list,parent,false)
        return PlayListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
    }

    class PlayListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var pName: TextView = itemView.find<TextView>(R.id.text_view_name)
        var pDetails: TextView = itemView.find<TextView>(R.id.text_view_info)
        var label: ImageView = itemView.find<ImageView>(R.id.image_view_album)
        var pOption: AppCompatImageView = itemView.find<AppCompatImageView>(R.id.image_button_action)
        var item: RelativeLayout? = itemView.find<RelativeLayout>(R.id.item)
    }

}