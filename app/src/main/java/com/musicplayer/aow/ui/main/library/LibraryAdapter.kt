package com.musicplayer.aow.ui.main.library

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.musicplayer.aow.ui.main.library.album.view.AlbumFragment
import com.musicplayer.aow.ui.main.library.artist.view.ArtistFragment
import com.musicplayer.aow.ui.main.library.home.DiscoveryFragment
import com.musicplayer.aow.ui.main.library.playlist.PlayListsFragment
import com.musicplayer.aow.ui.main.library.songs.view.AllSongsFragment

/**
 * Created by Arca on 11/19/2017.
 */

class LibraryAdapter(fm: FragmentManager, numbersOfTabs: Int) : FragmentStatePagerAdapter(fm) {

    private var numbersOfTabs = 0

    init {
        this.numbersOfTabs = numbersOfTabs
    }

    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                //return AllSongsFragment()
                return DiscoveryFragment()
            }
            1 -> {
                //return AlbumFragment()
                return AllSongsFragment()
            }
            2 -> {
                //return ArtistFragment()
                return AlbumFragment()
            }
            3 -> {
                //return PlayListsFragment()
                return ArtistFragment()
            }
            4 -> {
                return PlayListsFragment()
            }
            else -> return null
        }
    }

    override fun getCount(): Int {
        return numbersOfTabs
    }
}
