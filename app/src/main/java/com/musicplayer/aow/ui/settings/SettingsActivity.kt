package com.musicplayer.aow.ui.settings

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.LinearLayout
import com.github.nisrulz.sensey.Sensey
import com.musicplayer.aow.R
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.utils.ApplicationSettings
import com.musicplayer.aow.utils.StorageUtil
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.suke.widget.SwitchButton


/**
 * Created by Arca on 1/8/2018.
 */
class SettingsActivity : BaseActivity() {

    var sensor = MusicPlayerApplication.instance!!.sensory
    var appSettings = ApplicationSettings.instance
    private var shakeSwitchBtn: SwitchButton? = null
    private var flipSwitchBtn: SwitchButton? = null
    private var cacheSwitchBtn: SwitchButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)

        val tintManager = SystemBarTintManager(this)
        // enable status bar tint
        tintManager.isStatusBarTintEnabled = true
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true)

        // set a custom tint color for all system bars
        tintManager.setTintColor(R.color.translusent);
        // set a custom navigation bar resource
        tintManager.setNavigationBarTintResource(R.drawable.gradient_warning);
        // set a custom status bar drawable
        tintManager.setStatusBarTintResource(R.color.black);

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
        //cacheSwitchBtn = findViewById(R.id.cache_btn)

        shakeSettings(shakeSwitchBtn!!)
        flipSettings(flipSwitchBtn!!)
        cacheSettings(cacheSwitchBtn!!)

    }

    private fun shakeSettings(switch: SwitchButton){
        val action = appSettings!!.shakeaction
        val save: StorageUtil? = StorageUtil(applicationContext)
        val shakeSettingsState = save!!.loadStringValue(action)
        if (shakeSettingsState!!.equals("on")){
            switch.isChecked = true
        }
        //implement on checked on each radio button
        switch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                save.saveStringValue(action, "on")
                val shakeSettings = save.loadStringValue(action)
                Log.e("Shake", shakeSettings)
                sensor.startShakeDetection(appSettings!!.shakeGesture)
            }else {
                save.saveStringValue(action, "off")
                val shakeSettings = save.loadStringValue(action)
                Log.e("Shake", shakeSettings)
                Sensey.getInstance().stopShakeDetection(appSettings!!.shakeGesture)
            }
        }
    }

    private fun flipSettings(switch: SwitchButton){
        val action = appSettings!!.flipaction
        val save: StorageUtil? = StorageUtil(applicationContext)
        val shakeSettingsState = save!!.loadStringValue(action)
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
        val save: StorageUtil? = StorageUtil(applicationContext)
        val shakeSettingsState = save!!.loadStringValue(action)
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

    }


    companion object

}