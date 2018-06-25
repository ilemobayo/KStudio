package com.musicplayer.aow.ui.main.library.home.artist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceHolderSearchData
import com.musicplayer.aow.ui.main.search.SearchActivity
import com.musicplayer.aow.ui.main.search.adapter.SearchAdapter
import kotlinx.android.synthetic.main.activity_artist_online.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ArtistOnline : AppCompatActivity() {

    private var host_address: String? = "http://musixplaylb-1373597421.eu-west-2.elb.amazonaws.com/play"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_online)

        setupToolbar()

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }

        recycler_views.layoutManager =  LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false   )
        layout_body.visibility = View.INVISIBLE
        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("artist")
            if (data != null) {
                item_name.text = data
                item_element_list.text = intent.getStringExtra("des")
                callPlaylist(data)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
    }

    private fun callPlaylist(query: String){
        val url = "${host_address}/search/artist/data/${query}"
        doAsync {
            val callResponse = SoftCodeAdapter().getJsonString(applicationContext, url )
            onComplete {
                try {
                    val jsonArray = JSONObject(callResponse).getJSONArray("result")
                    callPlaylist(jsonArray)
                } catch ( e: IOException) {
                } catch (e: JsonSyntaxException){
                    //
                }
            }
        }
    }

    private fun createMainSectionData(section: ArrayList<SearchActivity.Search>){
        if(section.size > 0) {
            returned_msg.visibility = View.INVISIBLE
            progress_bar.visibility = View.INVISIBLE
            layout_body.visibility = View.VISIBLE
            recycler_views.adapter = SearchAdapter(applicationContext, section, false)
        }else{
            returned_msg.visibility = View.VISIBLE
            progress_bar.visibility = View.INVISIBLE
            layout_body.visibility = View.INVISIBLE
        }
    }

    private fun callPlaylist(jsonArray: JSONArray){
        val section_ = ArrayList<SearchActivity.Search>()
        for (i in 0..(jsonArray.length().minus(1))) {
            try {
                val item = jsonArray.getJSONObject(i)
                val playlistSection = SearchActivity.Search()
                playlistSection.type = item.getString("type")
                val memberItem = item.getJSONArray("member")
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
            } catch (e: JsonSyntaxException){
                //
            }
        }
        createMainSectionData(section_)
    }
}
