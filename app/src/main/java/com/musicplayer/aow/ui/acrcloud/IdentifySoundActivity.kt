package com.musicplayer.aow.ui.acrcloud

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.acrcloud.rec.sdk.ACRCloudClient
import com.acrcloud.rec.sdk.ACRCloudConfig
import com.acrcloud.rec.sdk.IACRCloudListener
import com.musicplayer.aow.R
import org.json.JSONException
import org.json.JSONObject
import java.io.File


class IdentifySoundActivity : AppCompatActivity(), IACRCloudListener {

    private var mClient: ACRCloudClient? = null
    private var mConfig: ACRCloudConfig? = null

    private var mVolume: TextView? = null
    var mResult: TextView? = null
    var tv_time: TextView? = null

    private var mProcessing = false
    private var initState = false

    private var path = ""

    private var startTime: Long = 0
    private var stopTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify_sound)

        path = (Environment.getExternalStorageDirectory().toString() + "/acrcloud/model")

        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }

        mVolume = findViewById<View>(R.id.volume) as TextView?
        mResult = findViewById<View>(R.id.result) as TextView?
        tv_time = findViewById<View>(R.id.time) as TextView?

        val startBtn = findViewById<View>(R.id.start) as Button
        startBtn.text = resources.getString(R.string.start)

        val stopBtn = findViewById<View>(R.id.stop) as Button
        stopBtn.text = resources.getString(R.string.stop)

        findViewById<View>(R.id.stop).setOnClickListener { stop() }

        val cancelBtn = findViewById<View>(R.id.cancel) as Button
        cancelBtn.text = resources.getString(R.string.cancel)

        findViewById<View>(R.id.start).setOnClickListener { start() }

        findViewById<View>(R.id.cancel).setOnClickListener { cancel() }


        this.mConfig = ACRCloudConfig()
        this.mConfig!!.acrcloudListener = this

        // If you implement IACRCloudResultWithAudioListener and override "onResult(ACRCloudResult result)", you can get the Audio data.
        //this.mConfig.acrcloudResultWithAudioListener = this;

        this.mConfig!!.context = this
        this.mConfig!!.host = "identify-eu-west-1.acrcloud.com"
        this.mConfig!!.dbPath = path // offline db path, you can change it with other path which this app can access.
        this.mConfig!!.accessKey = "b0842445b931c2512470ec2047ab80e4"
        this.mConfig!!.accessSecret = "svmYL02ghw3x9DRjogCodaUrGFzqgVXQsLcss1gP"
        this.mConfig!!.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP // PROTOCOL_HTTPS
        this.mConfig!!.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;

        this.mClient = ACRCloudClient()
        // If reqMode is REC_MODE_LOCAL or REC_MODE_BOTH,
        // the function initWithConfig is used to load offline db, and it may cost long time.
        this.initState = this.mClient!!.initWithConfig(this.mConfig)
        if (this.initState) {
            this.mClient!!.startPreRecord(3000) //start prerecord, you can call "this.mClient.stopPreRecord()" to stop prerecord.
        }
    }

    fun start() {
        if (!this.initState) {
            Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show()
            return
        }

        if (!mProcessing) {
            mProcessing = true
            mVolume?.text = ""
            mResult?.text = ""
            if (this.mClient == null || !this.mClient!!.startRecognize()) {
                mProcessing = false
                mResult?.text = "start error!"
            }
            startTime = System.currentTimeMillis()
        }
    }

    protected fun stop() {
        if (mProcessing && this.mClient != null) {
            this.mClient!!.stopRecordToRecognize()
        }
        mProcessing = false

        stopTime = System.currentTimeMillis()
    }

    protected fun cancel() {
        if (mProcessing && this.mClient != null) {
            mProcessing = false
            this.mClient!!.cancel()
            tv_time?.text = ""
            mResult?.text = ""
        }
    }

    // Old api
    override fun onResult(result: String) {
        if (this.mClient != null) {
            this.mClient!!.cancel()
            mProcessing = false
        }

        var tres = "\n"

        try {
            val j = JSONObject(result)
            val j1 = j.getJSONObject("status")
            val j2 = j1.getInt("code")
            if (j2 == 0) {
                val metadata = j.getJSONObject("metadata")
                //
                if (metadata.has("humming")) {
                    val hummings = metadata.getJSONArray("humming")
                    for (i in 0 until hummings.length()) {
                        val tt = hummings.get(i) as JSONObject
                        val title = tt.getString("title")
                        val artistt = tt.getJSONArray("artists")
                        val art = artistt.get(0) as JSONObject
                        val artist = art.getString("name")
                        tres = tres + (i + 1) + ".  " + title + "\n"
                    }
                }
                if (metadata.has("music")) {
                    val musics = metadata.getJSONArray("music")
                    for (i in 0 until musics.length()) {
                        val tt = musics.get(i) as JSONObject
                        val title = tt.getString("title")
                        val artistt = tt.getJSONArray("artists")
                        val art = artistt.get(0) as JSONObject
                        val artist = art.getString("name")
                        tres = tres + (i + 1) + ".  Title: " + title + "    Artist: " + artist + "\n"
                    }
                }
                if (metadata.has("streams")) {
                    val musics = metadata.getJSONArray("streams")
                    for (i in 0 until musics.length()) {
                        val tt = musics.get(i) as JSONObject
                        val title = tt.getString("title")
                        val channelId = tt.getString("channel_id")
                        tres = tres + (i + 1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n"
                    }
                }
                if (metadata.has("custom_files")) {
                    val musics = metadata.getJSONArray("custom_files")
                    for (i in 0 until musics.length()) {
                        val tt = musics.get(i) as JSONObject
                        val title = tt.getString("title")
                        tres = tres + (i + 1) + ".  Title: " + title + "\n"
                    }
                }
                tres = tres + "\n\n" + result
            } else {
                tres = result
            }
        } catch (e: JSONException) {
            tres = result
            e.printStackTrace()
        }

        mResult?.text = tres
    }

    override fun onVolumeChanged(volume: Double) {
        val time = (System.currentTimeMillis() - startTime) / 1000
        mVolume?.text = resources.getString(R.string.volume) + volume + "\n\n录音时间：" + time + " s"
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("MainActivity", "release")
        if (this.mClient != null) {
            this.mClient!!.release()
            this.initState = false
            this.mClient = null
        }
    }
}
