package com.musicplayer.aow.ui.main.library.home.discover.adapter.dynamic

import android.content.Context
import android.os.Build
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.main.library.home.discover.adapter.slider.SliderAdapter
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.collections.ArrayList


class SectionHeaderAdapter (private val mContext: Context, private val itemsList: PlaceholderData?) : RecyclerView.Adapter<SectionHeaderAdapter.SingleItemRowHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleItemRowHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_single_slider, null)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(holder: SingleItemRowHolder, i: Int) {
        val element = itemsList?.member

        holder.viewPager.adapter = SliderAdapter(mContext, itemsList)
        holder.indicator.setupWithViewPager(holder.viewPager, true)

        val timer = Timer()
        timer.scheduleAtFixedRate(SliderTimer(holder.viewPager, element?.size!!), 4000, 4000)

    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class SingleItemRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        var viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        var indicator = view.findViewById<TabLayout>(R.id.indicator)
    }


    class SliderTimer(viewPager: ViewPager, size: Int): TimerTask() {
        private val mViewPager = viewPager
        private val mSize = size
        private val mHandler = Handler()
        override fun run() {
            mHandler.post({
                if (mViewPager.currentItem < mSize.minus(1)) {
                    mViewPager.currentItem = mViewPager.currentItem + 1
                } else {
                    mViewPager.currentItem = 0
                }
            })
        }
    }
}