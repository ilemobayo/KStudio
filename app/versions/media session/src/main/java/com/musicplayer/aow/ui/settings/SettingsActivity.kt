package com.musicplayer.aow.ui.settings

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import com.github.nisrulz.sensey.Sensey
import com.musicplayer.aow.R
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.delegates.searchaudio.SearchAudio
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.utils.ApplicationSettings
import com.musicplayer.aow.utils.StorageUtil
import com.suke.widget.SwitchButton
import java.io.IOException


/**
 * Created by Arca on 1/8/2018.
 */
class SettingsActivity : BaseActivity() {

    var sensor = MusicPlayerApplication.instance!!.sensory
    var appSettings = ApplicationSettings.instance
    private var shakeSwitchBtn: SwitchButton? = null
    private var flipSwitchBtn: SwitchButton? = null
    private var cacheSwitchBtn: SwitchButton? = null
    private var fileServer: LinearLayout? = null
    private var searchBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.title = "Settings."
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        sensor.init(applicationContext)
        
        flipSwitchBtn = findViewById(R.id.flip_btn)
        shakeSwitchBtn = findViewById(R.id.shake_btn)
        cacheSwitchBtn = findViewById(R.id.cache_btn)
        fileServer = findViewById(R.id.file_server_btn)

        shakeSettings(shakeSwitchBtn!!)
        flipSettings(flipSwitchBtn!!)
        cacheSettings(cacheSwitchBtn!!)
        fileServerSettings(fileServer!!)

        searchBtn = findViewById(R.id.search_audio)
        searchBtn!!.setOnClickListener {
            val intent = SearchAudio.newIntent(applicationContext)
            startActivity(intent)
        }

    }

    private fun shakeSettings(switch: SwitchButton){
        val action = appSettings!!.shakeaction
        var save: StorageUtil? = StorageUtil(applicationContext)
        var shakeSettingsState = save!!.loadStringValue(action)
        if (shakeSettingsState!!.equals("on")){
            switch.isChecked = true
        }
        //implement on checked on each radio button
        switch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                save.saveStringValue(action, "on")
                var shakeSettings = save.loadStringValue(action)
                Log.e("Shake", shakeSettings)
                sensor.startShakeDetection(appSettings!!.shakeGesture)
            }else {
                save.saveStringValue(action, "off")
                var shakeSettings = save.loadStringValue(action)
                Log.e("Shake", shakeSettings)
                Sensey.getInstance().stopShakeDetection(appSettings!!.shakeGesture)
            }
        }
    }

    private fun flipSettings(switch: SwitchButton){
        val action = appSettings!!.flipaction
        var save: StorageUtil? = StorageUtil(applicationContext)
        var shakeSettingsState = save!!.loadStringValue(action)
        if (shakeSettingsState!!.equals("on")){
            switch.isChecked = true
        }
        //implement on checked on each radio button
        switch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                save.saveStringValue(action, "on")
                Sensey.getInstance().startFlipDetection(appSettings!!.flipGesture)
            }else {
                save.saveStringValue(action, "off")
                Sensey.getInstance().stopFlipDetection(appSettings!!.flipGesture)
            }
        }
    }

    private fun cacheSettings(switch: SwitchButton){
        val action = appSettings!!.cacheaction
        var save: StorageUtil? = StorageUtil(applicationContext)
        var shakeSettingsState = save!!.loadStringValue(action)
        if (shakeSettingsState!!.equals("on")){
            switch.isChecked = true
        }
        //implement on checked on each radio button
        switch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                save.saveStringValue(action, "on")
            }else {
                save.saveStringValue(action, "off")
            }
        }
    }

    private fun fileServerSettings(click: LinearLayout){
          click.setOnClickListener {
              //var intent = Intent(applicationContext,ServerActivity::class.java)
              //var intent = Intent(applicationContext,WiFiDirectActivity::class.java)
              //startActivity(intent)
          }


        try {
//            val CM = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val networkInfo = CM.allNetworkInfo
//            for (netInfo in networkInfo){
//                if (netInfo.typeName == "WIFI" || netInfo.typeName == "wifi") {
//                    if (netInfo.isConnected()){
//                        server = SimpleWebServer(GetDeviceipWiFiData(), 9090, File(Environment.getExternalStorageDirectory().absolutePath), false)
//                        Log.e("server", GetDeviceipWiFiData())
//                    }
//
//                }
//                if (netInfo.typeName == "MOBILE" || netInfo.typeName == "MOBILE".toLowerCase()){
//                    if (netInfo.isConnected()){
//                        server = SimpleWebServer(GetDeviceipMobileData(), 9090, File(Environment.getExternalStorageDirectory().absolutePath), false)
//                        Log.e("server", GetDeviceipMobileData())
//                    }
//                }
//            }


        }catch (e: Exception){
            Log.e("server", "err")
        } catch (e: IOException){
            Log.e("server", "err")
        }
    }


    companion object

}