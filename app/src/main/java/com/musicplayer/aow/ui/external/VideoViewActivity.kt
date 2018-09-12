package com.musicplayer.aow.ui.external

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.net.Uri.parse
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import com.musicplayer.aow.R


/**
 * Created by Arca on 1/11/2018.
 */
class VideoViewActivity : Activity(){

    // Declare variables
    private lateinit var videoview: VideoView

    // Insert your Video URL
    private lateinit var video:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the layout from video_main.xml
        setContentView(R.layout.activity_videoplayer)
        // Find your VideoView in your video_main.xml layout
        videoview = findViewById(R.id.VideoView)
        // Execute StreamVideo AsyncTask

        try {
            // Start the MediaController
            val mediacontroller = MediaController(
                    this@VideoViewActivity)
            mediacontroller.setAnchorView(videoview)
            // Get the URL from String VideoURL
            //paying audio from other apps
            val intent = intent
            if (intent != null) {
                // To get the action of the intent use
                val action = intent.action
                if (action != Intent.ACTION_VIEW) {
                    //
                }
                // To get the data use
                val data = intent.data
                if (data != null) {
                    video = parse(data.path)!!
                    //val video = parse(VideoURL)
                    videoview.setMediaController(mediacontroller)
                    videoview.setVideoURI(video)
                }
            }


        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }

        videoview.requestFocus()
        videoview.setOnPreparedListener {
            //pDialog.dismiss()
            videoview.start()
        }

    }
}