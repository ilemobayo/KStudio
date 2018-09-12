package com.musicplayer.aow.ui.main.library.home.podcast.adapter

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.musicplayer.aow.R
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.event.PlaySongEvent
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.main.library.home.podcast.model.Playlist
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import java.util.*

/**
 * Created by Arca on 2/17/2018.
 */
class PodcastAdapter(context: Context, activity: Activity, playList: ArrayList<Playlist>) : RecyclerView.Adapter<PodcastAdapter.AlbumViewHolder>() {
    val mSongModel = playList
    var context = context
    var activity = activity


    @TargetApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        var model = mSongModel[position]
        var albumName = model.title
        var albumArtist = model.source
        holder.albumName.text = albumName
        holder.albumArtist.text = albumArtist
        holder.label.text = "Podcast"

        doAsync {
            var generator = ColorGenerator.MATERIAL
            var color1 = generator.randomColor
            var icon = TextDrawable.builder().buildRect(model.title!!.substring(0, 1), color1)
            onComplete {
                Glide.with(context)
                        .load(model.imageUrl)
                        .into(holder.albumArt)

                holder.cardView.setOnClickListener {
                    val song = Track(model.title, model.title,
                            model.source, model.category, model.audioUrl,
                            model.audioDuration!!.toInt() * 1000, 1000, false, 0, "", model.imageUrl)
                    //RxBus.instance!!.playEvent.postValue(PlaySongEvent(song))
                    Player.instance?.play(PlayList(song))
                }
            }
        }

        //to set options
        holder.option.setOnClickListener {
            if (holder.view != null) {
               //
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.container_fish,parent,false)
        return AlbumViewHolder(view)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mSongModel.size
    }

    class AlbumViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var albumName: TextView = itemView.find<TextView>(R.id.cardname)
        var albumArtist: TextView = itemView.find<TextView>(R.id.cardart)
        var albumArt: ImageView = itemView.find<ImageView>(R.id.ivFish)
        var label: TextView = itemView.find<TextView>(R.id.text_view)
        var cardView: LinearLayout = itemView.find<LinearLayout>(R.id.card_view_container)
        var option: AppCompatImageView = itemView.find<AppCompatImageView>(R.id.item_button_action)
        var view: View = itemView
    }

}