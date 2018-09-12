package com.musicplayer.aow.ui.main.library.songs.view.`interface`

import android.view.View

/**
 * Created by Arca on 11/11/2017.
 */
interface CustomItemClickListener{
    fun onCustomItemClick(view: View, position: Int)
    fun onLongTap(index : Int)
    fun onTap(index : Int)
}