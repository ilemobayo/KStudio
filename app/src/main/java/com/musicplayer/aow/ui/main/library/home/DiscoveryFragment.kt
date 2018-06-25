package com.musicplayer.aow.ui.main.library.home

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.browse.adapter.RecyclerViewAdapter
import com.musicplayer.aow.ui.main.library.home.discover.data.AppDatabase
import kotlinx.android.synthetic.main.fragment_home_library.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException




/**
 * Created by Arca on 1/25/2018.
 */
class DiscoveryFragment : Fragment() {

    private var appDatabase: AppDatabase? = null
    private val host_address: String = "http://musixplaylb-1373597421.eu-west-2.elb.amazonaws.com/play"
    private var allSampleData: ArrayList<PlaceholderData>? = ArrayList()
    private var adapter: RecyclerViewAdapter? = null
    private var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerViewAdapter(context!!.applicationContext,allSampleData)
        music_update_recycler_view_body!!.adapter = adapter

        swipeContainer = view.findViewById(R.id.swipeContainer)
        // Setup and Handover data to recyclerview
        music_update_recycler_view_body!!.layoutManager =  LinearLayoutManager(
                context!!.applicationContext,
                LinearLayoutManager.VERTICAL,
                false   )
        music_update_recycler_view_body.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val lm = music_update_recycler_view_body.layoutManager as LinearLayoutManager
                swipeContainer?.isEnabled = lm.findFirstVisibleItemPosition() <= 0
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                music_update_recycler_view_body.layoutManager as LinearLayoutManager
            }
        })

        // Setup refresh listener which triggers new data loading
        swipeContainer?.setOnRefreshListener {
            music_update_recycler_view_body.visibility = View.VISIBLE
            network_error.visibility = View.INVISIBLE
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            callPlaylist()
            // To keep animation for 4 seconds

            Handler().postDelayed(Runnable {
                // Stop animation (This will be after 3 seconds)

                swipeContainer?.isRefreshing = false
            }, 4000) // Delay in millis
        }
        // Configure the refreshing colors
        swipeContainer?.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
        swipeContainer?.post({
            swipeContainer?.isRefreshing = true
            callPlaylist()
        })

//        podcast.setOnClickListener {
//            var intent = Intent(context!!.applicationContext, PodcastActivity::class.java)
//            startActivity(intent)
//        }
         appDatabase = AppDatabase.getsInstance(context?.applicationContext!!)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun isConnected(): Boolean {
        val command = "ping -c 1 zuezhome.com"
        return true
        //return Runtime.getRuntime().exec(command).waitFor() == 0
    }

    private fun callPlaylist(){
        val url = "${this.host_address}/discovery/placeholders/members/discovery"
        doAsync {
            val callResponse = SoftCodeAdapter().getJsonString(context!!, url )
            onComplete {
                try {
                    val jsonArray = JSONObject(callResponse).getJSONArray("result")
                    prepareData(jsonArray)
                } catch ( e: IOException) {
                    //e.printStackTrace()
                } catch ( e: JSONException){
                    Log.e(this.javaClass.name, e.message)
                }
            }
        }
    }

    private fun prepareData(jsonArray: JSONArray){
        val section__ = ArrayList<PlaceholderData>()
        section__.add(PlaceholderData())
        for (i in 0..(jsonArray.length().minus(1))) {
            try {
                val item = jsonArray.getJSONObject(i)
                val playlistSection = PlaceholderData()
                playlistSection._id = item.getString("_id")
                playlistSection.name = item.getString("name")
                playlistSection.type = item.getString("type")
                playlistSection.public = item.getBoolean("public")
                playlistSection.owner = item.getString("owner")
                playlistSection.picture = item.getString("picture")
                if (playlistSection.picture == "" || playlistSection.picture == null){
                    playlistSection.picture = "http://zuezhome.com/play/ic_logo.png"
                }
                playlistSection.dateCreated = item.getString("date_created")
                val memberItem = item.getJSONArray("member")
                for (x in 0..(memberItem?.length()!!.minus(1))) {
                    val members = memberItem.getJSONObject(x)
                    val mMemberItem = PlaceholderData()
                    mMemberItem._id = members.getString("_id")
                    mMemberItem.name = members.getString("name")
                    mMemberItem.type = members.getString("type")
                    mMemberItem.owner = members.getString("owner")
                    mMemberItem.picture = members.getString("picture")
                    if (mMemberItem.picture == "" || mMemberItem.picture == null){
                        mMemberItem.picture = "http://zuezhome.com/play/ic_logo.png"
                    }
                    mMemberItem.location = members.getString("location")
                    mMemberItem.description = members.getString("description")
                    mMemberItem.dateCreated = members.getString("date_created")
                    playlistSection.member.add(mMemberItem)
                }
                playlistSection.description = item.getString("description")
                section__.add(playlistSection)
            }catch (e: JSONException){
                //Log.e(this.javaClass.name, e.message)
            }
        }
        //appDatabase?.playListObjectDAO()?.insertMultiplePlayList(section__)
        getMainSectionChildren(section__)
        swipeContainer?.isRefreshing = false
    }

    private fun getMainSectionChildren(sectionMain:ArrayList<PlaceholderData>){
        allSampleData = sectionMain
        adapter = RecyclerViewAdapter(context!!.applicationContext,allSampleData)
        music_update_recycler_view_body!!.adapter = adapter
        music_update_recycler_view_body.visibility = View.VISIBLE
        Handler().postDelayed(Runnable {
            // Stop animation (This will be after 3 seconds)
            swipeContainer?.isRefreshing = false
        }, 4000) // Delay in millis
    }

}