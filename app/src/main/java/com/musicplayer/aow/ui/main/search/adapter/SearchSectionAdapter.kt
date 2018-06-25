package com.musicplayer.aow.ui.main.search.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceHolderSearchData
import com.musicplayer.aow.ui.main.library.home.artist.ArtistOnline
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import java.util.*


/**
 * Created by Arca on 11/28/2017.
 */
class SearchSectionAdapter(private val mContext: Context, private val itemsList: ArrayList<PlaceHolderSearchData>?, var limit: Boolean = true) : RecyclerView.Adapter<SearchSectionAdapter.SingleItemRowHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_single_card_linear, null)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(holder: SingleItemRowHolder, i: Int) {
        val singleItem = itemsList!![i]
        holder.tvTitle.text = singleItem.name
        holder.artist.text = singleItem.owner

        doAsync {
            val img = Glide.with(mContext)
                    .load(singleItem.picture).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.gradient_info)
                    .into(150, 150)
                    .get()
            onComplete {
                holder.itemImage.setImageBitmap(img)
            }
        }

        holder.itemBody.setOnClickListener{
            if (singleItem.type == "track" || singleItem.type == "playlist" || singleItem.type == "album") {
                val gsonBuilder = GsonBuilder().create()
                val jsonFromPojo = gsonBuilder.toJson(singleItem)
                val intent = Intent(mContext, BrowseActivity::class.java)
                intent.putExtra("data", jsonFromPojo)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(mContext, intent, null)
            }else if(singleItem.type == "artist"){
                val intent = Intent(mContext, ArtistOnline::class.java)
                intent.putExtra("artist", singleItem.name)
                intent.putExtra("des", singleItem.description)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(mContext, intent, null)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (null != itemsList){
            if (limit){
                if (itemsList.size > 20){
                    20
                }else{
                    itemsList.size
                }
            } else{
                itemsList.size
            }

        }else { 0 }
    }

    inner class SingleItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle = view.find<TextView>(R.id.tvTitle)
        var artist = view.find<TextView>(R.id.artist)
        var itemImage = view.find<ImageView>(R.id.itemImage)
        var itemBody = view.find<LinearLayout>(R.id.body)
    }

}