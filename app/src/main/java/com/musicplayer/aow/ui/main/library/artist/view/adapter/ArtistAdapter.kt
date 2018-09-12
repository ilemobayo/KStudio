package com.musicplayer.aow.ui.main.library.artist.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.ui.main.library.activities.ArtistSongs
import com.musicplayer.aow.utils.images.BitmapDraws
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import java.util.*

/**
 * Created by Arca on 11/27/2017.
 */
class ArtistAdapter(var context: Context, var activity: Activity, artistList: ArrayList<Artists>?) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    private var view:View? = null
    private var mArtistModel = artistList

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {

        val model = mArtistModel?.get(position)
        val numOfSong = model?.numberOfSongs
        val albumArtist = model?.artist_name
        holder.noOfSongs.text = if (numOfSong?.toInt()!! <= 1){
            numOfSong.toString().plus(" Track")
        }else{
            numOfSong.toString().plus(" Tracks")
        }
        holder.albumArtist.text = albumArtist

        doAsync {
            val alb = Injection.provideContext()!!
                    .contentResolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(
                            MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums.ARTIST + "=?",
                    arrayOf(model.artist_name!!),
                    null)
            onComplete {
                if (alb.moveToFirst()) {
                    val data = alb.getString(alb.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                    val albumArt = BitmapDraws.createFromPath(data)
                    if (albumArt != null) {
                        holder.albumArt.setImageDrawable(albumArt)
                    }else{
                        holder.albumArt.setImageResource(R.drawable.gradient_danger)
                    }
                }else{
                    holder.albumArt.setImageResource(R.drawable.gradient_danger)
                }
                alb.close()
            }

        }

        holder.cardView.setOnClickListener {
            val intent = Intent(context, ArtistSongs::class.java)
            intent.putExtra("com.musicplayer.aow.artist.name", albumArtist)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, intent, null)
        }

        //to set options
        holder.option.setOnClickListener {
            val songs = SoftCodeAdapter().getArtistTracks(context, model.artist_id!!, true)
            val context = holder.view.context
            val mBottomSheetDialog = BottomSheetDialog(context)
            val sheetView =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_modal_dialog_album, null)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.show()
            mBottomSheetDialog.setOnDismissListener {
                //perform action on close
            }

            val play = sheetView!!.find<LinearLayout>(R.id.menu_item_play_now)
            val playNext = sheetView.find<LinearLayout>(R.id.menu_item_play_next)
            val queue = sheetView.find<LinearLayout>(R.id.menu_item_add_to_queue)
            val playlist = sheetView.find<LinearLayout>(R.id.menu_item_add_to_play_list)
            val delete = sheetView.find<LinearLayout>(R.id.menu_item_delete)

            play.setOnClickListener {
                Player.instance?.play(PlayList(songs), 0)
                mBottomSheetDialog.dismiss()
            }

            playNext.setOnClickListener {
                Player.instance!!.insertnext(Player.instance!!.mPlayList!!.playingIndex,songs)
                mBottomSheetDialog.dismiss()
            }

            queue.setOnClickListener {
                Player.instance!!.insertnext(Player.instance!!.mPlayList!!.numOfSongs,songs)
                mBottomSheetDialog.dismiss()
            }

            playlist.setOnClickListener {
                mBottomSheetDialog.dismiss()
                //Dialog with ListView
                val nContext = view!!.context
                val mSelectPlaylistDialog = BottomSheetDialog(nContext)
                val sheetView =  LayoutInflater.from(nContext).inflate(R.layout.custom_dialog_select_playlist, null)
                val mylist = sheetView.find<RecyclerView>(R.id.recycler_playlist_views)

                SoftCodeAdapter().addSongToPlaylist(nContext, mylist, mSelectPlaylistDialog, Track(), songs, true)

                mSelectPlaylistDialog.setContentView(sheetView)
                mSelectPlaylistDialog.show()
                mSelectPlaylistDialog.setOnDismissListener {}
            }

            delete.setOnClickListener {
                SoftCodeAdapter().deleteArtist(context, model.artist_id!!, true)
                mBottomSheetDialog.dismiss()
            }
        }
    }

    fun swapCursor(artistList: ArrayList<Artists>?): ArrayList<Artists>? {
        if (mArtistModel === artistList) {
            return null
        }
        val oldCursor = mArtistModel
        this.mArtistModel = artistList
        if (artistList != null) {
            this.notifyDataSetChanged()
        }
        return oldCursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.artist_card_view,parent,false)
        return ArtistViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mArtistModel?.size!!
    }

    class ArtistViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var noOfSongs: TextView = itemView.find(R.id.artist_no_songs)
        var albumArtist: TextView = itemView.find(R.id.artist_name)
        var albumArt: ImageView = itemView.find(R.id.artist_album_art)
        var cardView: CardView = itemView.find(R.id.card_view_container_artist)
        var option: AppCompatImageView = itemView.find(R.id.item_button_action)
        var view: View = itemView
    }

}