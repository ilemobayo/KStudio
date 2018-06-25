package com.musicplayer.aow.ui.base.adapter

interface IAdapterView<T> {

    fun bind(item: Any?, position: Int)
}
