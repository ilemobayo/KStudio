package com.musicplayer.aow.ui.main.library.songs


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.ui.main.library.songs.Adapter.SongListAdapter
import com.musicplayer.aow.ui.mvvm.viewmodel.SongsViewModel
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.musicplayer.aow.utils.CursorDB
import kotlinx.android.synthetic.main.fragment_all_songs.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import rx.subscriptions.CompositeSubscription

class AllSongsFragment : Fragment(){

    private val MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val WHERE = (MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
            + MediaStore.Audio.Media.SIZE + ">0" )
    private val ORDER_BY = MediaStore.Audio.Media.TITLE + " ASC"
    private val PROJECTIONS = arrayOf(
            MediaStore.Audio.Media.DATA, // the real path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE)
    private var songs: PlayList = PlayList()
    private var adapter: SongListAdapter? = null
    private var mSubscriptions: CompositeSubscription? = null
    val songsViewModel: SongsViewModel
        get() = ViewModelProviders.of(this).get(SongsViewModel::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mSubscriptions == null) {
            mSubscriptions = CompositeSubscription()
        }
        super.onViewCreated(view, savedInstanceState)

        recycler_views.layoutManager = LinearLayoutManager(context!!.applicationContext)
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        recycler_views.addItemDecoration(
                DividerItemDecoration(
                        activity!!.getDrawable(
                                R.drawable.drawble_divider
                        ),
                        false,
                        true)
        )
        songsViewModel.songsResult?.observe(this, Observer<ArrayList<Song>> { t ->
            songs = PlayList(t)
            recycler_views.adapter = SongListAdapter(context!!, songs, mSubscriptions, activity!! )
        })
    }

}
