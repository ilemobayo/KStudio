package com.musicplayer.aow.delegates.softcode.adapters

import android.support.v7.util.DiffUtil
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData

class DiffUtilCallback(
        private var newData: ArrayList<PlaceholderData>,
        private var oldData: ArrayList<PlaceholderData>): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    override fun areItemsTheSame(oldItemPosition:Int, newItemPosition:Int):Boolean {
        return oldData[oldItemPosition]._id === newData[newItemPosition]._id
    }
    override fun areContentsTheSame(oldItemPosition:Int, newItemPosition:Int):Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition:Int, newItemPosition:Int): Any {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)!!
    }
}