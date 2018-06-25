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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete

class SectionHeaderAdapter (private val mContext: Context, private val itemsList: PlaceholderData?) : RecyclerView.Adapter<SectionHeaderAdapter.SingleItemRowHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_single_slider, null)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(holder: SingleItemRowHolder, i: Int) {
        val singleItem = itemsList

        doAsync {
            val img = Glide.with(mContext)
                    .load(singleItem?.member?.get(0)?.picture).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(550, 550)
                    .get()
            onComplete {
                holder.itemImage.setImageBitmap(img)
                holder.shimmer
            }
        }

        val gsonBuilder = GsonBuilder().create()
        val jsonFromPojo = gsonBuilder.toJson(singleItem)
        val intent = Intent(mContext, BrowseActivity::class.java)
        intent.putExtra("data", jsonFromPojo)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        holder.shimmer.setOnClickListener{
            ContextCompat.startActivity(mContext, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class SingleItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemBody = view.find<CardView>(R.id.image_card)
        var itemImage = view.find<ImageView>(R.id.itemImage)
        var shimmer = view.find<ShimmerFrameLayout>(R.id.shimmer_view_container)
    }

}