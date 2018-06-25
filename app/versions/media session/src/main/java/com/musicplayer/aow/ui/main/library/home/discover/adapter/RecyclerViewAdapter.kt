package com.musicplayer.aow.ui.browse.adapter

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.browse.model.sectiondatamodel.SectionDataModel
import org.jetbrains.anko.find
import java.util.*


/**
 * Created by Arca on 11/28/2017.
 */
class RecyclerViewAdapter(val mContext: Context, val dataList: ArrayList<SectionDataModel>?) : RecyclerView.Adapter<RecyclerViewAdapter.ItemRowHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, null)
        return ItemRowHolder(v)
    }

    override fun onBindViewHolder(itemRowHolder: ItemRowHolder, i: Int) {
        val sectionName = dataList!!.get(i).headerTitle
        val singleSectionItems = dataList.get(i).allItemsInSection

        itemRowHolder.itemTitle.text = sectionName
        val itemListDataAdapter = SectionListDataAdapter(mContext.applicationContext, singleSectionItems)

        itemRowHolder.recycler_view_list.setHasFixedSize(true)
//        itemRowHolder.recycler_view_list.layoutManager = LinearLayoutManager(mContext.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        itemRowHolder.recycler_view_list.layoutManager = GridLayoutManager(mContext!!.applicationContext, 2)
        itemRowHolder.recycler_view_list.adapter = itemListDataAdapter

        itemRowHolder.btnMore.setOnClickListener{
               // Toast.makeText(mContext, " " + sectionName, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return if (null != dataList) dataList.size else 0
    }

    inner class ItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemTitle: TextView
        var recycler_view_list: RecyclerView
        var btnMore: Button

        init {
            this.itemTitle = view.find<TextView>(R.id.itemTitle)
            this.recycler_view_list = view.find<RecyclerView>(R.id.recycler_view_list)
            this.btnMore = view.find<Button>(R.id.btnMore)
        }

    }

}