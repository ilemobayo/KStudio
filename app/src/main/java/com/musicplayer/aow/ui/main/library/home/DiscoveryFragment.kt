package com.musicplayer.aow.ui.main.library.home

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.SoftCodeAdapter
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.main.library.home.discover.adapter.RecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_home_library.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.json.JSONException
import org.json.JSONObject
import android.widget.Toast
import com.google.gson.Gson
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.DiscoveryDatabase
import com.musicplayer.aow.delegates.data.db.model.DiscoveryModel
import java.io.*




/**
 * Created by Arca on 1/25/2018.
 */
class DiscoveryFragment : Fragment() {

    private var discoveryDatabase: DiscoveryDatabase? = null
    private val host_address: String = "http://musixplaylb-1373597421.eu-west-2.elb.amazonaws.com/play"
    private var allSampleData: ArrayList<PlaceholderData>? = ArrayList()
    private var adapter: RecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoveryDatabase = DiscoveryDatabase.getsInstance(context?.applicationContext!!)

        adapter = RecyclerViewAdapter(context!!.applicationContext, ArrayList())
        music_update_recycler_view_body!!.adapter = adapter

        // Setup and Handover data to recyclerview
        music_update_recycler_view_body!!.layoutManager =  LinearLayoutManager(
                context!!.applicationContext,
                LinearLayoutManager.VERTICAL,
                false   )
        music_update_recycler_view_body.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lm = music_update_recycler_view_body.layoutManager as LinearLayoutManager
                swipeContainer.isEnabled = lm.findFirstVisibleItemPosition() <= 0
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                music_update_recycler_view_body.layoutManager as LinearLayoutManager
            }
        })

        AppExecutors.instance?.diskIO()?.execute {
            val discovery = discoveryDatabase?.discoveryDAO()?.fetchDiscoveryLiveData()
            discovery?.observe(this, Observer<DiscoveryModel> {
                if (it == null){
                    callRefreshDiscovery()
                } else {
                    getDiscoveryJson(it)
                }
            })
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer?.setOnRefreshListener {
            callRefreshDiscovery()
            // To keep animation for 4 seconds
            Handler().postDelayed(Runnable {
                // Stop animation (This will be after 3 seconds)
                swipeContainer?.isEnabled = true
                swipeContainer?.isRefreshing = false
            }, 4000) // Delay in millis
        }
        // Configure the refreshing colors
        swipeContainer?.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)


//        podcast.setOnClickListener {
//            var intent = Intent(context!!.applicationContext, PodcastActivity::class.java)
//            startActivity(intent)
//        }

    }

    @Throws(InterruptedException::class, IOException::class)
    private fun isConnected(): Boolean {
        val command = "ping -c 1 zuezhome.com"
//        return true
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }

    private fun callRefreshDiscovery(){
        if (!isConnected()){
            Toast.makeText(context, context?.getText(R.string.network_connection_error), Toast.LENGTH_SHORT).show()
            return
        } else {
            val url = "${this.host_address}/discovery/placeholders/members/discovery"
            doAsync {
                val callResponse = SoftCodeAdapter().getJsonString(context!!, url)
                onComplete {
                    try {
                        val jsonObject = JSONObject(callResponse)
                        AppExecutors.instance?.diskIO()?.execute {
                            val discovery = discoveryDatabase?.discoveryDAO()?.fetchDiscovery()
                            if (discovery == null) {
                                val json = Gson().fromJson(jsonObject.toString(), DiscoveryModel::class.java)
                                if (json != null) {
                                    discoveryDatabase?.discoveryDAO()?.insert(json)
                                } else {

                                }
                            } else {
                                val json = Gson().fromJson(jsonObject.toString(), DiscoveryModel::class.java)
                                if (json != null) {
                                    discoveryDatabase?.discoveryDAO()?.update(json)
                                } else {

                                }
                            }
                        }
                    } catch (e: IOException) {
                    } catch (e: JSONException) {
                    }
                }
            }
        }
    }

    private fun getDiscoveryJson(data: DiscoveryModel){
        try {
            prepareData(data)
        } catch ( e: IOException) {
            Log.e(this.javaClass.name, e.message)
        } catch ( e: JSONException){
            Log.e(this.javaClass.name, e.message)
        }
    }

    private fun prepareData(discovery: DiscoveryModel){
        if (discovery.result == null || discovery.result?.size == 0){
            return
        }
        val section__ = ArrayList<PlaceholderData>()
        section__.add(PlaceholderData())
        for (i in 0..(discovery.result?.size!!.minus(1))) {
            try {
                val item = discovery.result?.get(i)!!
                val playlistSection = PlaceholderData()
                playlistSection._id = "${item.id!!}.${item.name!!}.${item.id!!}.${item.dateCreated!!}.${item.owner!!}"
                playlistSection.name = item.name!!
                playlistSection.type = item.type!!
                playlistSection.public = item.public!!
                playlistSection.owner = item.owner!!
                playlistSection.picture = item.picture!!
                if (playlistSection.picture == ""){
                    playlistSection.picture = "http://zuezhome.com/play/ic_logo.png"
                }
                playlistSection.dateCreated = item.dateCreated!!
                val memberItem = item.member
                for (x in 0..(memberItem?.size!!.minus(1))) {
                    val members = memberItem.get(x)
                    val mMemberItem = PlaceholderData()
                    mMemberItem._id = members.id!!
                    mMemberItem.name = members.name!!
                    mMemberItem.type = members.type!!
                    mMemberItem.owner = members.owner!!
                    mMemberItem.picture = members.picture!!
                    if (mMemberItem.picture == ""){
                        mMemberItem.picture = "http://zuezhome.com/play/ic_logo.png"
                    }
                    mMemberItem.location = members.location!!
                    mMemberItem.description = members.description!!
                    mMemberItem.dateCreated = members.dateCreated!!
                    playlistSection.member.add(mMemberItem)
                }
                playlistSection.description = item.description!!
                section__.add(playlistSection)
            }catch (e: JSONException){
                Log.e(this.javaClass.name, e.message)
            }
        }
        getMainSectionChildren(section__)
    }

    private fun getMainSectionChildren(sectionMain:ArrayList<PlaceholderData>){
//        allSampleData = sectionMain
//        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(allSampleData!!, sectionMain))
//        diffResult.dispatchUpdatesTo(adapter)
        adapter?.swapCursor(sectionMain)
    }

}