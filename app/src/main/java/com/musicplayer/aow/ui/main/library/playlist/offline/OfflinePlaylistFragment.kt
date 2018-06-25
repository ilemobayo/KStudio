package com.musicplayer.aow.ui.main.library.playlist.offline


import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.ui.widget.DividerItemDecoration
import com.musicplayer.aow.utils.CursorDB
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.onComplete


class OfflinePlaylistFragment : Fragment() {
    val TAG = this.javaClass.name
    private val MEDIA_URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
    private val WHERE = null
    private val ORDER_BY = null
    private val PROJECTIONS = null
    internal var progress_bar: ProgressBar? = null
    internal var recycler_view: RecyclerView? = null
    private var btn_create_playlist: Button? = null
    var adapter: OfflinePlaylistAdapter? = null
    var modelData:ArrayList<PlayList> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //create favorite playlist if not created before
        SoftCodeAdapter().getFavoritesId(context!!)
        return inflater.inflate(R.layout.fragment_offline_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OfflinePlaylistAdapter(context!!.applicationContext, modelData, this.layoutInflater)
        loaderManager.initLoader(0, null, mLoaderCallbacks)

        progress_bar = view.find(R.id.progress_bar)
        progress_bar!!.visibility = View.INVISIBLE
        recycler_view = view.find(R.id.recycler_playlist_views)
        recycler_view!!.visibility = View.VISIBLE
        btn_create_playlist = view.find(R.id.create_playlist)
        btn_create_playlist!!.setOnClickListener {
            showChangeLangDialog()
        }

        loadData()
    }



    private fun showChangeLangDialog() {
        try {
            val dialogBuilder = AlertDialog.Builder(context!!, android.R.style.Theme_Material_Light_Dialog)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_dialog_input, null)
            dialogBuilder.setView(dialogView)

            val edt = dialogView.find<EditText>(R.id.edit1)
            edt.setText(getString(R.string.default_playlist_name))

            dialogBuilder.setTitle(getString(R.string.create_playlist_dialog_title)).setIcon(R.drawable.ic_play_now_rename)
            dialogBuilder.setPositiveButton(getString(R.string.save), { dialog, which ->
                //do something with edt.getText().toString();
                val newPlayList = PlayList()
                newPlayList.name = edt.text.toString()
                SoftCodeAdapter().createPlaylist(Injection.provideContext()!!, edt.text.toString())
            })
            dialogBuilder.setNegativeButton(getString(R.string.cancel), { dialog, which -> })
            dialogBuilder.create().show()
        }catch (e: NullPointerException){
            //
        }
    }

    fun loadData(){
        //Setup layout manager
        val layoutManager = PreCachingLayoutManager(activity!!)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(activity!!))
        recycler_view!!.addItemDecoration(
                DividerItemDecoration(
                        activity!!.getDrawable(
                                R.drawable.drawble_divider
                        ),
                        false,
                        true)
        )
        recycler_view!!.setHasFixedSize(true)
        recycler_view!!.layoutManager = layoutManager
        recycler_view!!.adapter = adapter
    }

    private val mLoaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return CursorLoader(context!!.applicationContext, MEDIA_URI,
                    PROJECTIONS, WHERE, null,
                    ORDER_BY)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            modelData = ArrayList()
            if (data != null) {
                doAsync {
                    while (data.moveToNext()) {
                        modelData.add(CursorDB().cursorToPlayList(data))
                    }
                    onComplete {
                        adapter?.swapCursor(modelData)
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            adapter?.swapCursor(null)
        }
    }


}
