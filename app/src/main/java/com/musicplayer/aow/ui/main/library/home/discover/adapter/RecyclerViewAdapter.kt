package com.musicplayer.aow.ui.main.library.home.discover.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
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
import com.musicplayer.aow.delegates.softcode.adapters.AutoFitGridLayoutManager
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.browse.adapter.SectionListDataAdapter
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import com.musicplayer.aow.ui.main.library.home.discover.adapter.dynamic.SectionGridDataAdapter
import com.musicplayer.aow.ui.main.library.home.discover.adapter.dynamic.SectionHeaderAdapter
import org.jetbrains.anko.find
import java.util.*


/**
 * Created by Arca on 11/28/2017.
 */
class RecyclerViewAdapter(val mContext: Context, var dataList: ArrayList<PlaceholderData>?) : RecyclerView.Adapter<RecyclerViewAdapter.ItemRowHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, null)
        return ItemRowHolder(v)
    }

    override fun onBindViewHolder(itemRowHolder: ItemRowHolder, position: Int) {
        val model = dataList!!.get(position)
        val sectionName = model.name
        val singleSectionItems = model.member

        itemRowHolder.itemTitle.text = sectionName

        val gsonBuilder = GsonBuilder().create()
        val jsonFromPojo = gsonBuilder.toJson(dataList?.get(position))
        val intent = Intent(mContext, BrowseActivity::class.java)
        intent.putExtra("data", jsonFromPojo)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        itemRowHolder.btnMore.setOnClickListener{
            ContextCompat.startActivity(mContext, intent, null)
        }

        itemRowHolder.recycler_view_list.setHasFixedSize(true)
        when (dataList?.indexOf(model)) {
            0 -> {
                itemRowHolder.apply{
                    linearLayout.apply {
                        setPadding(0, 1, 0, 0)
                    }
                    itemTitle.visibility = View.GONE
                    btnMore.visibility = View.GONE
                    recycler_view_list.visibility = View.GONE
                    val itemListDataAdapter = SectionHeaderAdapter(mContext.applicationContext, model)
                    recycler_view_list.apply {
                        layoutManager = GridLayoutManager(mContext, 1)
                        adapter = itemListDataAdapter
                    }
                }
            }
            1 -> {
                itemRowHolder.apply{
                    linearLayout.apply {
                        val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, -10, 0, 0)
                        linearLayout.layoutParams = params
                        setPadding(0,0,0,5)
                    }
                    itemTitle.visibility = View.GONE
                    btnMore.visibility = View.GONE
                    recycler_view_list.visibility = View.VISIBLE
                    val itemListDataAdapter = SectionHeaderAdapter(mContext.applicationContext, model)
                    recycler_view_list.apply {
                        layoutManager = GridLayoutManager(mContext, 1)
                        adapter = itemListDataAdapter
                    }
                }
            }
            3 -> {
                itemRowHolder.itemTitle.visibility = View.VISIBLE
                itemRowHolder.btnMore.visibility = View.VISIBLE
                itemRowHolder.recycler_view_list.visibility = View.VISIBLE
                itemRowHolder.recycler_view_list.setPadding(0, 0, 0, 30)
                val itemListDataAdapter = SectionGridDataAdapter(mContext.applicationContext, singleSectionItems, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    itemRowHolder.recycler_view_list.layoutManager = AutoFitGridLayoutManager(mContext, 350)
                }else {
                    itemRowHolder.recycler_view_list.layoutManager = AutoFitGridLayoutManager(mContext, 150)
                }
                itemRowHolder.recycler_view_list.adapter = itemListDataAdapter
            }
            7 -> {
                itemRowHolder.itemTitle.visibility = View.VISIBLE
                itemRowHolder.btnMore.visibility = View.VISIBLE
                itemRowHolder.recycler_view_list.visibility = View.VISIBLE
                itemRowHolder.recycler_view_list.setPadding(10, 0, 0, 30)
                val itemListDataAdapter = SectionListDataAdapter(mContext.applicationContext, singleSectionItems)
                itemRowHolder.recycler_view_list.layoutManager = LinearLayoutManager(mContext.applicationContext, LinearLayoutManager.VERTICAL, false)
                itemRowHolder.recycler_view_list.adapter = itemListDataAdapter
            }
            else -> {
                itemRowHolder.itemTitle.visibility = View.VISIBLE
                itemRowHolder.btnMore.visibility = View.VISIBLE
                itemRowHolder.recycler_view_list.visibility = View.VISIBLE
                itemRowHolder.recycler_view_list.setPadding(0, 0, 0, 30)
                val itemListDataAdapter = SectionGridDataAdapter(mContext.applicationContext, singleSectionItems)
                itemRowHolder.recycler_view_list.layoutManager = LinearLayoutManager(mContext.applicationContext, LinearLayoutManager.HORIZONTAL, false)
                itemRowHolder.recycler_view_list.adapter = itemListDataAdapter
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

    fun swapCursor(playList: ArrayList<PlaceholderData>?): ArrayList<PlaceholderData>? {
        if (dataList === playList) {
            return null
        }
        val oldCursor = dataList
        this.dataList = playList
        if (playList != null) {
            this.notifyDataSetChanged()
        }
        return oldCursor
    }

    inner class ItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var linearLayout: LinearLayout = view.find(R.id.linearLayout)
        var itemTitle: TextView = view.find(R.id.itemTitle)
        var btnMore: Button = view.find(R.id.btnMore)
        var recycler_view_list: RecyclerView = view.find(R.id.recycler_view_list)
    }

}