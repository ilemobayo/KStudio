package com.musicplayer.aow.ui.main.library.playlist


import android.content.DialogInterface
import android.os.Bundle
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
import com.musicplayer.aow.bus.RxBus
import com.musicplayer.aow.delegates.data.model.PlayList
import com.musicplayer.aow.delegates.data.source.AppRepository
import com.musicplayer.aow.delegates.event.ReloadEvent
import com.musicplayer.aow.ui.base.BaseFragment
import com.musicplayer.aow.utils.DeviceUtils
import com.musicplayer.aow.utils.layout.PreCachingLayoutManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription


class PlayListsFragment : BaseFragment(){

    internal var progress_bar: ProgressBar? = null
    internal var recycler_view: RecyclerView? = null
    private val mSubscriptions: CompositeSubscription? = null
    internal var btn_create_playlist: Button? = null

    var playListAdapter:PlayListAdapter? = null
    var playList:List<PlayList>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_play_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress_bar = view.find(R.id.progress_bar)
        recycler_view = view.find(R.id.recycler_playlist_views)
        btn_create_playlist = view.find<Button>(R.id.create_playlist)
        //create playlist
        btn_create_playlist!!.setOnClickListener {
            showChangeLangDialog()
        }

        loadPlayLists()
    }


    private fun showChangeLangDialog() {
        try {
            val dialogBuilder = AlertDialog.Builder(context!!, android.R.style.Theme_Material_Light_Dialog)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_dialog_input, null)
            dialogBuilder.setView(dialogView)

            val edt = dialogView.find<EditText>(R.id.edit1)
            edt.setText("New playlist")

            dialogBuilder.setTitle("Playlist name").setIcon(R.drawable.ic_play_now_rename)
            dialogBuilder.setPositiveButton("Save", DialogInterface.OnClickListener { dialog, which ->
                //do something with edt.getText().toString();
                if (edt.text.toString() != null) {
                    var newPlayList = PlayList()
                    newPlayList.name = edt.text.toString()
                    createPlayList(newPlayList)
                }
            })
            dialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> })
            dialogBuilder.create().show()
        }catch (e: NullPointerException){
            //
        }
    }


    fun loadData(playlist: List<PlayList>?){
        var playlistModelData = playlist
        //get audio from shearPref
        if (playlistModelData == null){
            progress_bar!!.visibility = View.INVISIBLE
        }else {
            progress_bar!!.visibility = View.INVISIBLE
            //sort the song list in ascending order
            playList = playlistModelData.sortedWith(compareBy({ (it.name)!!.toLowerCase() }))
            //Save to database
            playListAdapter = PlayListAdapter(context!!.applicationContext, playList!!, this.layoutInflater)
            //Setup layout manager
            val layoutManager = PreCachingLayoutManager(activity!!)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(activity!!))
            recycler_view!!.setHasFixedSize(true)
            recycler_view!!.layoutManager = layoutManager
            recycler_view!!.adapter = playListAdapter
        }
    }

    fun loadPlayLists(){
        val subscription = AppRepository().playLists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<List<PlayList>>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(playLists: List<PlayList>) {
                        loadData(playLists)
                    }
                })
        mSubscriptions?.add(subscription)
    }

    private fun createPlayList(playList: PlayList) {
        val subscription = AppRepository().create(playList)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<PlayList>() {
                    override fun onStart() {}

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(result: PlayList) {
                        loadPlayLists()
                    }
                })
        mSubscriptions?.add(subscription)
    }

    // RXBus Events
    override fun subscribeEvents(): Subscription {
        return RxBus.instance?.toObservable()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnNext({ o ->
                    when(o){
                        is PlayListAction -> {
                            loadPlayLists()
                        }
                        is ReloadEvent -> {
                            doAsync {
                                loadPlayLists()
                            }
                        }
                    }
                })?.subscribe(RxBus.defaultSubscriber())!!
    }

}
