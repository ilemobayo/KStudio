package com.musicplayer.aow.ui.main.library.playlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.musicplayer.aow.ui.main.library.playlist.offline.OfflinePlaylistFragment
import com.musicplayer.aow.ui.main.library.playlist.online.OnlinePlaylistFragment


/**
 * Created by Arca on 12/6/2017.
 */
class PlayListAdapter (fm: FragmentManager, numbersOfTabs: Int) : FragmentStatePagerAdapter(fm) {

    private var numbersOfTabs = 0

    init {
        this.numbersOfTabs = numbersOfTabs
    }

    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                return OfflinePlaylistFragment()
            }
            1 -> {
                return OnlinePlaylistFragment()
            }
            else -> return null
        }
    }

    override fun getCount(): Int {
        return numbersOfTabs
    }
}