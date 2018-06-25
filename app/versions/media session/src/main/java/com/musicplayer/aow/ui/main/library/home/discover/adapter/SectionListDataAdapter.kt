package com.musicplayer.aow.ui.browse.adapter

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
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.browse.model.singleitemmodel.SingleItemModel
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete
import java.util.*


/**
 * Created by Arca on 11/28/2017.
 */
class SectionListDataAdapter(private val mContext: Context, private val itemsList: ArrayList<SingleItemModel>?) : RecyclerView.Adapter<SectionListDataAdapter.SingleItemRowHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_single_card, null)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(holder: SingleItemRowHolder, i: Int) {
        val singleItem = itemsList!!.get(i)
        holder.tvTitle.text = singleItem.name
        holder.artist.text = singleItem.artist

        doAsync {
            var img = Glide.with(mContext)
                    .load(singleItem.url).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(150, 150)
                    .get()
            onComplete {
                holder.itemImage.setImageBitmap(img)
            }
        }


        holder.itemBody.setOnClickListener{
            val intent = Intent(mContext, BrowseActivity::class.java)
            intent.putExtra("com.musicplayer.aow.section", singleItem.toString())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(mContext, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return if (null != itemsList) itemsList.size else 0
    }

    inner class SingleItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle = view.find<TextView>(R.id.tvTitle)
        var artist = view.find<TextView>(R.id.artist)
        var itemImage = view.find<ImageView>(R.id.itemImage)
        var itemBody = view.find<CardView>(R.id.image_card)
    }

}