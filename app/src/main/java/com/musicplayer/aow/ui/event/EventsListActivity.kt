package com.musicplayer.aow.ui.event

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.event.adapter.EventListAdapter
import com.musicplayer.aow.ui.event.adapter.Model
import kotlinx.android.synthetic.main.activity_events_list.*
import java.util.*

class EventsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_list)
        progress_bar.visibility = View.VISIBLE
        recycler_views.layoutManager = LinearLayoutManager(this)
        var data = ArrayList<Model>()
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        data.add(Model("Games Dealer", "Eko hotel"))
        recycler_views.adapter = EventListAdapter(this,data)
        progress_bar.visibility = View.INVISIBLE
        recycler_views.visibility = View.VISIBLE
    }
}
