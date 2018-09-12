package com.musicplayer.aow.ui.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
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
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black, theme)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        sensor.init(applicationContext)

        flipSwitchBtn = findViewById(R.id.flip_btn)
        shakeSwitchBtn = findViewById(R.id.shake_btn)

        try {
            shakeSettings(shakeSwitchBtn!!)
            flipSettings(flipSwitchBtn!!)
        }catch (e: java.lang.Exception){

        }


    }

    private fun shakeSettings(switch: SwitchButton){
        val action = appSettings!!.shakeaction
        val save: StorageUtil? = StorageUtil(applicationContext)
        val shakeSettingsState = save?.loadStringValue(action)
        //implement on checked on each radio button
        switch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                save?.saveStringValue(action, "on")
                val shakeSettings = save?.loadStringValue(action)
                Log.e("Shake", shakeSettings)
                sensor.startShakeDetection(appSettings?.shakeGesture)
            }else {
                save?.saveStringValue(action, "off")
                val shakeSettings = save?.loadStringValue(action)
                Log.e("Shake", shakeSettings)
                sensor.stopShakeDetection(appSettings?.shakeGesture)
            }
        }
        if (shakeSettingsState?.equals("on")!!){
            switch.isChecked = true
        }
    }

    private fun flipSettings(switch: SwitchButton){
        val action = appSettings!!.flipaction
        val save: StorageUtil? = StorageUtil(applicationContext)
        val shakeSettingsState = save!!.loadStringValue(action)
        //implement on checked on each radio button
        switch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                save.saveStringValue(action, "on")
                sensor.startFlipDetection(appSettings!!.flipGesture)
            }else {
                save.saveStringValue(action, "off")
                sensor.stopFlipDetection(appSettings!!.flipGesture)
            }
        }
        if (shakeSettingsState!!.equals("on")){
            switch.isChecked = true
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


    companion object

}