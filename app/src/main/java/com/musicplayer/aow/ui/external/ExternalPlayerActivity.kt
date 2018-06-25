package com.musicplayer.aow.ui.external

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.utils.FileUtilities
import com.musicplayer.aow.utils.TimeUtils
import com.musicplayer.aow.utils.images.BitmapDraws
import java.io.File

/**
 * Created by Arca on 11/29/2017.
 */
class ExternalPlayerActivity : BaseActivity(), View.OnClickListener{

    private var imageViewAlbum: ImageView? = null
    private var textViewName: TextView? = null
    private var textViewProgress: TextView? = null
    private var textViewDuration: TextView? = null
    private var seekBarProgress: SeekBar? = null
    private var buttonPlayToggle: ImageView? = null
    private var playBackVolume: SeekBar? = null
    private var audioManager: AudioManager? = null

    private var mPlayer: MediaPlayer? = MediaPlayer()

    private val mHandler = Handler()

    private val mProgressCallback = object : Runnable {
        override fun run() {
            if (mPlayer!!.isPlaying) {
                val progress = (seekBarProgress!!.max * (mPlayer!!.currentPosition.toFloat() / currentSongDuration.toFloat())).toInt()
                updateProgressTextWithDuration(mPlayer!!.currentPosition)
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
            if (mPlayer != null) {
                return mPlayer!!.duration
            }else{
                return 0
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_external_player)


        volumeControlStream = AudioManager.STREAM_MUSIC

        imageViewAlbum = findViewById(R.id.image_view_album)
        textViewProgress = findViewById(R.id.text_view_progress)
        textViewName = findViewById(R.id.text_view_name)
        textViewDuration = findViewById(R.id.text_view_duration)
        buttonPlayToggle = findViewById(R.id.button_play_toggle)
        seekBarProgress = findViewById(R.id.seek_bar)

        //Play/Pause button
        buttonPlayToggle!!.setOnClickListener{
            onPlayToggleAction()
        }

        //PlayBack Speed
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
 

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
                if (mPlayer!!.isPlaying) {
                    mHandler.removeCallbacks(mProgressCallback)
                    mHandler.post(mProgressCallback)
                }
            }
        })

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
                val albumArt = BitmapDraws.createFromPath(data.path)
                if (albumArt != null) {
                    imageViewAlbum!!.setImageDrawable(albumArt)
                }else{
                    //imageViewAlbum!!.setImageResource(R.drawable.gradient_danger)
                }
                textViewName?.text = FileUtilities.fileToMusic(File(data.path))?.displayName
                playSong(data.path)
            }else{
                val location = intent.getStringExtra("location")
                if(location != null){
                    textViewName?.text = FileUtilities.fileToMusic(File(location))?.displayName
                    playSong(location)
                }
            }
        }

    }


    override fun onClick(v: View) {
        //nothing
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE)
                //Raise the Volume Bar on the Screen
                playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                //Adjust the Volume
                audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE)
                //Lower the VOlume Bar on the Screen
                playBackVolume!!.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
            else -> return false
        }
    }


    override fun onStart() {
        super.onStart()
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
        }
    }

    override fun onStop() {
        mPlayer!!.stop()
        mPlayer!!.reset()
        mHandler.removeCallbacks(mProgressCallback)
        finish()
        super.onStop()
    }

    override fun onDestroy() {
        finish()
        super.onDestroy()
    }

    // Click Events
    private fun onPlayToggleAction() {
        if (mPlayer == null) return

        if (mPlayer!!.isPlaying) {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
            mPlayer!!.pause()
        } else {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
            mPlayer!!.start()
            mHandler.removeCallbacks(mProgressCallback)
            mHandler.post(mProgressCallback)
        }
    }


    private fun playSong(file: String) {
        if (mPlayer == null) return
        mPlayer!!.setDataSource(file)
        mPlayer!!.prepare()
        mPlayer!!.start()
        //finished updating
        seekBarProgress!!.progress = 0
        seekBarProgress!!.isEnabled = mPlayer?.isPlaying!!
        textViewProgress!!.setText(R.string.mp_music_default_duration)
        if (mPlayer?.isPlaying!!) {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_pause)
            textViewDuration!!.text = TimeUtils.formatDuration(mPlayer!!.duration)
        } else {
            buttonPlayToggle!!.setImageResource(R.drawable.ic_play)
            textViewDuration!!.setText(R.string.mp_music_default_duration)
        }
    }

    private fun updateProgressTextWithProgress(progress: Int) {
        val targetDuration = getDuration(progress)
        textViewProgress!!.text = TimeUtils.formatDuration(targetDuration)
    }

    private fun updateProgressTextWithDuration(duration: Int) {
        textViewProgress!!.text = TimeUtils.formatDuration(duration)
    }

    private fun seekTo(duration: Int) {
        mPlayer!!.seekTo(duration)
    }

    private fun getDuration(progress: Int): Int {
        return (currentSongDuration * (progress.toFloat() / seekBarProgress!!.max)).toInt()
    }


    companion object {

        private const val TAG = "ExternalPlayerActivity"

        // Update seek bar every second
        private const val UPDATE_PROGRESS_INTERVAL: Long = 1000

    }
}
