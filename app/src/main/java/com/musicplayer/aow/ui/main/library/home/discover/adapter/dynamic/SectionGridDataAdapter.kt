package com.musicplayer.aow.ui.main.library.home.discover.adapter.dynamic

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import com.bumptech.glide.request.RequestOptions



class SectionGridDataAdapter (private val mContext: Context, private val itemsList: List<PlaceholderData>?, var shortList: Boolean = false) : RecyclerView.Adapter<SectionGridDataAdapter.SingleItemRowHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_single_card, null)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(holder: SingleItemRowHolder, i: Int) {
        val singleItem = itemsList!!.get(i)
        holder.tvTitle.text = singleItem.name
        holder.artist.text = singleItem.owner

       Glide.with(mContext)
               .load(singleItem.picture)
                .apply(
                        RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .dontAnimate()
                                .dontTransform()
                )
                .into(holder.itemImage)

        val gsonBuilder = GsonBuilder().create()
        val jsonFromPojo = gsonBuilder.toJson(singleItem)
        val intent = Intent(mContext, BrowseActivity::class.java)
        intent.putExtra("data", jsonFromPojo)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        holder.itemBody.setOnClickListener{
            ContextCompat.startActivity(mContext, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return if (null != itemsList){
                    if (shortList){
                        if (itemsList.size > 9){
                            9
                        }else {
                            itemsList.size
                        }
                    }else{
                        itemsList.size
                    }
                }else { 0 }
    }

    inner class SingleItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle = view.find<TextView>(R.id.tvTitle)
        var artist = view.find<TextView>(R.id.artist)
        var itemImage = view.find<ImageView>(R.id.itemImage)
        var itemBody = view.find<CardView>(R.id.image_card)
    }

}