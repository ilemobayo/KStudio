package com.musicplayer.aow.ui.event

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.musicplayer.aow.R
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        location.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=20+Ademola+Adetokunbo, Victorial+Island,+Lagos+Nigeria&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.`package` = "com.google.android.apps.maps"
            startActivity(mapIntent)
        }

    }

}
