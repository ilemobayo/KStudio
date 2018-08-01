package com.musicplayer.aow.ui.main.search

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceHolderSearchData
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.main.search.adapter.SearchAdapter
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class SearchActivity : BaseActivity(), SearchView.OnQueryTextListener {

    var searchView: SearchView? = null
    private var host_address: String? = "http://musixplaylb-1373597421.eu-west-2.elb.amazonaws.com/play"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupToolbar()

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }

        val tintManager = SystemBarTintManager(this)
        // enable status bar tint
        tintManager.isStatusBarTintEnabled = true
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true)

        // set a custom tint color for all system bars
        tintManager.setTintColor(R.color.translusent)
        // set a custom navigation bar resource
        tintManager.setNavigationBarTintResource(R.drawable.gradient_warning)
        // set a custom status bar drawable
        tintManager.setStatusBarTintResource(R.color.black)

        search_recyclerview.layoutManager =  LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false   )

        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("query")
            if (data != null) {
                callPlaylist(data)
            }
        }
    }

    private fun createMainSectionData(section: ArrayList<Search>){
        if (section.size <= 0){
            init.visibility = View.VISIBLE
            search_msg.text = "Not found, please try again..."
        }else {
            init.visibility = View.GONE
            search_recyclerview.adapter = SearchAdapter(applicationContext, section)
        }
    }

    override fun setTitle(title: CharSequence) {
        //
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView?.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        // Here is where we are going to implement the filter logic
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        callPlaylist(query)
        search_msg.text = "Searching..."
        return false
    }

    private fun callPlaylist(query: String){
        val url = "${host_address}/search/${query}"
        doAsync {
            val callResponse = SoftCodeAdapter().getJsonString(applicationContext, url )
            onComplete {
                try {
                    val jsonArray = JSONObject(callResponse).getJSONArray("result")
                    callPlaylist(jsonArray)
                } catch ( e: IOException) {
                    init.visibility = View.VISIBLE
                    search_msg.text = "Search error, please try again..."
                } catch (e: JSONException){
                    init.visibility = View.VISIBLE
                    search_msg.text = "Search error, please try again..."
                }
            }
        }
    }

    private fun callPlaylist(jsonArray: JSONArray){
        val section_ = ArrayList<Search>()
        for (i in 0..(jsonArray.length().minus(1))) {
            try {
                val item = jsonArray.getJSONObject(i)
                val playlistSection = Search()
                playlistSection.type = item.getString("type")
                val memberItem = item.getJSONArray("items")
                for (x in 0..(memberItem?.length()!!.minus(1))) {
                    val members = memberItem.getJSONObject(x).toString()
                    val gson = Gson()
                    val placeholder = gson.fromJson(members, PlaceHolderSearchData::class.java)
                    playlistSection.item.add(placeholder)
                }
                if (playlistSection.item.size > 0){
                    section_.add(playlistSection)
                }
                
            }catch (e: JSONException){
                Log.e(this.javaClass.name, e.localizedMessage)
            }catch (e: JsonSyntaxException){
                Log.e(this.javaClass.name, e.localizedMessage)
            }
        }
        createMainSectionData(section_)
    }

    class Search {
        var type: String? = ""
        var item: ArrayList<PlaceHolderSearchData> = ArrayList()
    }
}

