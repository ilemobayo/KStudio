package com.musicplayer.aow.ui.main.library.home.podcast

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import android.view.WindowManager
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.main.library.home.podcast.adapter.PodcastAdapter
import com.musicplayer.aow.ui.main.library.home.podcast.model.Playlist
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_podcast.*
import org.json.JSONArray
import java.util.*
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority


class PodcastActivity : BaseActivity(), SearchView.OnQueryTextListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val window = window
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.statusBarColor = resources.getColor(R.color.black)
        }

        setupToolbar()

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }

        callPlaylist()
    }

    private fun createMainSectionData(section: ArrayList<Playlist>){
        podcast.layoutManager = GridLayoutManager(applicationContext, 2) as RecyclerView.LayoutManager?
        podcast.adapter = PodcastAdapter(applicationContext, this,section)
    }

    override fun setTitle(title: CharSequence) {
        //
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        // Here is where we are going to implement the filter logic
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        var intent = Intent(applicationContext, PodcastSearchActivity::class.java)
        intent.putExtra("query", query)
        startActivity(intent)
        //Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show()
        return false
    }

    fun callPlaylist(){
        val url = "http://api.ottoradio.com/v1/podcasts?count=50"
        var __section__ = ArrayList<Playlist>()
        // prepare the Request

        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        // do anything with response
                        val data = response
                        for(i in 0..(data.length() - 1)){
                            val item = data.getJSONObject(i)
                            val playlistSection = Playlist()
                            playlistSection.id = item.getString("id")
                            playlistSection.title = item.getString("title")
                            playlistSection.audioUrl = item.getString("audio_url")
                            playlistSection.audioDuration = item.getDouble("audio_duration")
                            playlistSection.category = item.getString("category")
                            playlistSection.imageUrl = item.getString("image_url")
                            playlistSection.publishedAt = item.getString("published_at")
                            playlistSection.description = item.getString("description")
                            playlistSection.sourceImageUrl = item.getString("source_image_url")
                            playlistSection.source = item.getString("source")
                            __section__.add(playlistSection)
                        }
                        createMainSectionData(__section__)
                        progress_bar.visibility = View.INVISIBLE
                    }

                    override fun onError(error: ANError) {
                        // handle error
                    }
                })

//        Rx2AndroidNetworking.get(url)
//                .build()
//                .jsonArrayObservable
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : Observable<JSONArray>() {
//                    override fun onError(e: Throwable?) {
////                        val inflater = layoutInflater
////                        val toastLayout = inflater.inflate(R.layout.custom_toast, findViewById<View>(R.id.custom_toast_layout) as ViewGroup)
////                        val toast = Toast(applicationContext)
////                        toast.duration = Toast.LENGTH_SHORT
////                        toast.view = toastLayout
////                        toast.setText(e!!.localizedMessage)
////                        toast.show()
//                    }
//
//                    override fun onNext(t: JSONArray?) {
//                        var data = t!!
//                        for(i in 0..(data.length() - 1)){
//                            var item = data.getJSONObject(i)
//                            var playlistSection = Playlist()
//                            playlistSection.id = item.getString("id")
//                            playlistSection.title = item.getString("title")
//                            playlistSection.audioUrl = item.getString("audio_url")
//                            playlistSection.audioDuration = item.getDouble("audio_duration")
//                            playlistSection.category = item.getString("category")
//                            playlistSection.imageUrl = item.getString("image_url")
//                            playlistSection.publishedAt = item.getString("published_at")
//                            playlistSection.description = item.getString("description")
//                            playlistSection.sourceImageUrl = item.getString("source_image_url")
//                            playlistSection.source = item.getString("source")
//                            __section__.add(playlistSection)
//                        }
//                        createMainSectionData(__section__)
//                    }
//
//                    override fun onCompleted() {
//                        progress_bar.visibility = View.INVISIBLE
//                    }
//                })

    }
}
