package com.musicplayer.aow.ui.base.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.delegates.data.model.Song


abstract class ListAdapter<T, V : IAdapterView<*>>(private val mContext: Context, private var mData: MutableList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemClickListener: OnItemClickListener? = null
        private set
    var itemLongClickListener: OnItemLongClickListener? = null
        private set
    var lastItemClickPosition = RecyclerView.NO_POSITION
        private set

    var data: MutableList<Any>
        get() = mData
        set(data) {
            mData = data
        }

    protected abstract fun createView(context: Context): V

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = createView(mContext) as View
        val holder = object : RecyclerView.ViewHolder(itemView) {

        }
        if (itemClickListener != null) {
            itemView.setOnClickListener {
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    lastItemClickPosition = position
                    itemClickListener!!.onItemClick(position)
                }
            }
        }
        if (itemLongClickListener != null) {
            itemView.setOnLongClickListener {
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemLongClickListener!!.onItemClick(position)
                }
                false
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemView = holder.itemView as V
        itemView.bind(getItem(position), position)
    }

    override fun getItemCount(): Int {
        return if (mData == null) 0 else mData!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun addData(data: MutableList<Song>){
        mData!!.addAll(data)
    }

    open fun getItem(position: Int): Any {
        return mData!![position]
    }

    fun clear() {
        if (mData != null)
            mData!!.clear()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        itemLongClickListener = listener
    }
}
