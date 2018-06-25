package com.musicplayer.aow.ui.main

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.musicplayer.aow.ui.base.BaseFragment

class MainPagerAdapter(fm: FragmentManager, private val mTitles: Array<String>?, private val mFragments: Array<BaseFragment?>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): BaseFragment? {
        return mFragments[position]!!
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mTitles!![position]
    }

    override fun getCount(): Int {
        return mTitles?.size!!
    }
}
