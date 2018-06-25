package com.musicplayer.aow.ui.main.library.home.podcast

import android.os.Build
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.musicplayer.aow.R
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.main.library.home.podcast.adapter.PodcastAdapter
import com.musicplayer.aow.ui.main.library.home.podcast.model.Playlist
import kotlinx.android.synthetic.main.activity_podcast.*
import org.json.JSONObject
import java.util.*

class PodcastSearchActivity : BaseActivity(), SearchView.OnQueryTextListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_search)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.black)
        }

        setupToolbar()

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }

        podcast.layoutManager = GridLayoutManager(applicationContext, 2)

        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("query")
            if (data != null) {
                callPlaylist(data)
            }
        }
    }

    private fun createMainSectionData(section: ArrayList<Playlist>){
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
        callPlaylist(query)
        //Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show()
        return false
    }

    fun callPlaylist(query: String){
        val url = "http://api.ottoradio.com/v1/playlist?query={$query}"
        var __section__ = ArrayList<Playlist>()
        // prepare the Request
        var getRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    // display response
                    var data = response.getJSONArray("stories")
                    for(i in 0..(data.length() - 1)){
                        var item = data.getJSONObject(i)
                        var playlistSection = Playlist()
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
                },
                Response.ErrorListener { response ->
                    Log.e("Error.Response", response.toString())
                }
        )

        MusicPlayerApplication.instance!!.getRequestQueue()!!.add(getRequest)
    }
}

