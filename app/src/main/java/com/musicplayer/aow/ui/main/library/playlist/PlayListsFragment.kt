package com.musicplayer.aow.ui.main.library.playlist


import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import kotlinx.android.synthetic.main.fragment_play_lists.*


class PlayListsFragment : Fragment(){

    val TAG = this.javaClass.name

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_play_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tablayout()
        viewPager()
    }

    private fun tablayout(){
        playlist_tablayout.addTab(playlist_tablayout.newTab().setText("Offline"))
        playlist_tablayout.addTab(playlist_tablayout.newTab().setText("Online"))
        playlist_tablayout.tabGravity = TabLayout.GRAVITY_FILL

        playlist_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                playlist_viewpager.setCurrentItem(tab?.position!! , false)
            }

        })
    }

    private fun viewPager(){
        val adapter = PlayListAdapter(activity!!.supportFragmentManager,playlist_tablayout.tabCount)
        playlist_viewpager.disableScroll(false)
        playlist_viewpager.offscreenPageLimit = 2
        playlist_viewpager.adapter = adapter
        playlist_viewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(playlist_tablayout))
    }


}
