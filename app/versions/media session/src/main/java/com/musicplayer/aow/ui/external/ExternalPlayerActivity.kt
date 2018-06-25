package com.musicplayer.aow.ui.external

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.utils.FileUtilities
import com.musicplayer.aow.utils.TimeUtils
import java.io.File

/**
 * Created by Arca on 11/29/2017.
 */
class ExternalPlayerActivity : Activity(){

    private var imageViewAlbum: ImageView? = null
    private var textViewName: TextView? = null
    private var textViewProgress: TextView? = null
    private var textViewDuration: TextView? = null
    private var seekBarProgress: SeekBar? = null
    private var buttonPlayToggle: ImageView? = null

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var mPlayer = Player.instance
    private val mHandler = Handler()

    private val mProgressCallback = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val progress = (seekBarProgress!!.max * (mediaPlayer.currentPosition.toFloat() / currentSongDuration.toFloat())).toInt()
                updateProgressTextWithDuration(mediaPlayer.currentPosition)
                if (progress >= 0 && progress <= seekBarProgress!!.max) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        seekBarProgress!!.setProgress(progress, true)
                    } else {
                        seekBarProgress!!.progress = progress
                    }
                    mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL)
                }
            }
        }
    }

    private val currentSongDuration: Int
        get() {
            if (mediaPlayer != null) {
                    val currentSong = mediaPlayer
                    var duration = 0
                    if (currentSong != null) {
                        duration = currentSong.duration
                    }
                    return duration
            }else{
                return 0
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_external_player)
        //add your objects/initialization here

        //to make the layout looks poped up
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        window.setLayout((width * .9).toInt(), (height * .22).toInt())
        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = -20
        window.attributes = params

        textViewProgress = findViewById<TextView>(R.id.external_text_view_progress)
        textViewName = findViewById<TextView>(R.id.external_audio_view_name)
        textViewDuration = findViewById<TextView>(R.id.external_text_view_duration)
        imageViewAlbum = findViewById<ImageView>(R.id.external_image_view_album)
        buttonPlayToggle = findViewById<ImageView>(R.id.external_button_play_toggle)
        seekBarProgress = findViewById<SeekBar>(R.id.external_seek_bar)

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
                if (mPlayer!!.isPlaying){
                    mPlayer!!.pause()
                }
                mediaPlayer!!.reset()
                mediaPlayer!!.setDataSource(FileUtilities.fileToMusic(File(data.path))!!.path)
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()


                //Drawable Text
                var generator = ColorGenerator.MATERIAL // or use DEFAULT
                // generate random color
                var color1 = generator.randomColor
                var icon = TextDrawable.builder().buildRect(data.path!!.substring(0,5), color1)
                imageViewAlbum!!.setImageDrawable(icon)
            }
        }

        //Play/Pause button
        buttonPlayToggle!!.setOnClickListener{
            onPlayToggleAction()
        }

        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying){
                textViewDuration!!.text = TimeUtils.formatDuration(mediaPlayer.duration)
            }
        }

        seekBarProgress!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateProgressTextWithProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mProgressCallback)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekTo(getDuration(seekBar.progress))
                if (mediaPlayer.isPlaying) {
                    mHandler.removeCallbacks(mProgressCallback)
                    mHandler.post(mProgressCallback)
                }
            }
        })

    }

    private fun releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release()
        }

        if (!mPlayer!!.isPlaying){
            mPlayer!!.play()
        }
    }

    override fun onStop() {
        releasePlayer()
        mHandler.removeCallbacks(mProgressCallback)
        //we stop service
        super.onStop()
    }

    override fun onBackPressed() {
        releasePlayer()
        finish()
    }

    // Click Events
    private fun onPlayToggleAction() {
        if (mediaPlayer == null) return

        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }else{
            mediaPlayer.start()
        }
        updatePlayToggle(mediaPlayer.isPlaying)
    }

    private fun updateProgressTextWithProgress(progress: Int) {
        val targetDuration = getDuration(progress)
        textViewProgress!!.text = TimeUtils.formatDuration(targetDuration)
    }

    private fun updateProgressTextWithDuration(duration: Int) {
        textViewProgress!!.text = TimeUtils.formatDuration(duration)
    }

     fun seekTo(progress: Int): Boolean {

        val currentSong = mediaPlayer
        if (currentSong != null) {
            if (currentSong.duration <= progress) {
                //
            } else {
                mediaPlayer.seekTo(progress)
            }
            return true
        }
        return false
    }

    private fun getDuration(progress: Int): Int {
        return (currentSongDuration * (progress.toFloat() / seekBarProgress!!.max)).toInt()
    }

    private fun updatePlayToggle(play: Boolean) {
        buttonPlayToggle!!.setImageResource(if (play) R.drawable.ic_pause else R.drawable.ic_play)
    }

    companion object {
        // Update seek bar every second
        private val UPDATE_PROGRESS_INTERVAL: Long = 1000
    }

}
