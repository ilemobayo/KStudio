package com.musicplayer.aow.ui.event.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.musicplayer.aow.R
import com.musicplayer.aow.R.id.*
import com.musicplayer.aow.ui.event.MapsActivity
import org.jetbrains.anko.find
import java.util.*

/**
 * Created by Arca on 3/11/2018.
 */
class EventListAdapter(
        context: Context,
        list: ArrayList<Model>) : RecyclerView.Adapter<EventListAdapter.ListViewHolder>()
{
    var context = context
    var view: View? = null
    val TAG = "EventListAdapter"
    private val mModel = list

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        var model = mModel[position]
        holder.name.text = model.mName
        holder.loaction.text = model.mLocation

        holder.mListItem.setOnClickListener {
            Log.e(TAG, "clicked")
            val intent = Intent(context, MapsActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }

    }

    private fun removeAt(position: Int, song: Model) {
        mModel.remove(song)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mModel.size)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_event_list, parent, false)
        return ListViewHolder(view!!)
    }

    //we get the count of the list
    override fun getItemCount(): Int {
        return mModel.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemView = itemView
        var name: TextView = itemView.find<TextView>(text_view_name)
        var loaction: TextView = itemView.find<TextView>(text_view_artist)
        var albumArt: ImageView = itemView.find<ImageView>(image_view_file)
        var mListItem: CardView = itemView.find<CardView>(card_view_container)
    }

}