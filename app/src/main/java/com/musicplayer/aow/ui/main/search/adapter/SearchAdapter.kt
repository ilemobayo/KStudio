package com.musicplayer.aow.ui.main.search.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import com.musicplayer.aow.ui.main.search.SearchActivity
import org.jetbrains.anko.find
import java.util.*






/**
 * Created by Arca on 11/28/2017.
 */
class SearchAdapter(val mContext: Context,
                    val dataList: ArrayList<SearchActivity.Search>?,
                    var limit: Boolean = true) : RecyclerView.Adapter<SearchAdapter.ItemRowHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, null)
        return ItemRowHolder(v)
    }

    override fun onBindViewHolder(itemRowHolder: ItemRowHolder, position: Int) {
        val model = dataList!![position]
        val sectionName = model.type
        val singleSectionItems = model.item

        itemRowHolder.itemTitle.text = sectionName

        itemRowHolder.btnMore.setOnClickListener{
            val gsonBuilder = GsonBuilder().create()
            val jsonFromPojo = gsonBuilder.toJson(dataList[position])
            val intent = Intent(mContext, BrowseActivity::class.java)
            intent.putExtra("data", jsonFromPojo)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(mContext, intent, null)
        }
        itemRowHolder.btnMore.visibility = View.INVISIBLE

        itemRowHolder.recycler_view_list.setHasFixedSize(true)

        val itemListDataAdapter = SearchSectionAdapter(mContext.applicationContext, singleSectionItems, limit)
        itemRowHolder.recycler_view_list.layoutManager = LinearLayoutManager(mContext.applicationContext, LinearLayoutManager.VERTICAL, false)
        itemRowHolder.recycler_view_list.adapter = itemListDataAdapter
    }

    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

    inner class ItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var linearLayout: LinearLayout = view.find(R.id.linearLayout)
        var itemTitle: TextView = view.find(R.id.itemTitle)
        var btnMore: Button = view.find(R.id.btnMore)
        var recycler_view_list: RecyclerView = view.find(R.id.recycler_view_list)
    }

}