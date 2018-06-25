package com.musicplayer.aow.ui.main.library.songs


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.ui.base.BaseFragment
import com.musicplayer.aow.ui.main.library.songs.Adapter.SongListAdapter
import kotlinx.android.synthetic.main.fragment_all_songs.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription


class AllSongsFragment : BaseFragment() {
    
    private val mSubscriptions: CompositeSubscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_all_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_views.layoutManager = LinearLayoutManager(context!!.applicationContext)
        progress_bar.visibility = View.VISIBLE
        loadAllSongs()
    }

    fun loadData(playList: PlayList) {
        data(playList)
    }

    fun data(songs: PlayList){
        progress_bar.visibility = View.INVISIBLE
        songs.songs.sortedWith(compareBy({ (it.title)!!.toLowerCase() }))
        recycler_views.adapter = SongListAdapter(context!!, songs, activity!! )
        //recycler_views.adapter.setHasStableIds(true)
        //display the list
        progress_bar.visibility = View.INVISIBLE
        recycler_views.visibility = View.VISIBLE
    }

    private fun loadAllSongs() {
        val subscription = AppRepository().playlist(resources.getString(R.string.mp_play_list_songs))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                        loadData(result)
                    }
                })
        mSubscriptions?.add(subscription)
    }

}
