package com.musicplayer.aow.ui.eq.model

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.utils.StorageUtil

class EqModel() {

    val storageUtil = StorageUtil()
    var eq: Equalizer? = null
    var bb: BassBoost? = null
    var rev: PresetReverb? = null
    var vrt: Virtualizer? = null

    var min_level = 0
    var max_level = 100

    val MAX_SLIDERS = 5 // Must match the XML layout

    fun updateEqualizer(audioSession: Int = 0) : Equalizer? {
        if (eq == null){
            eq = Equalizer(0, audioSession)
            updateEnabled(eq!!)
        }
        for (i in 0 until MAX_SLIDERS) {
            var level = storageUtil.loadStringValue("Band$i")

            if (eq != null) {
                updateEnabled(eq!!)
                if (level.equals("empty", true)) {
                    level = eq!!.getBandLevel(i.toShort()).toString()
                    storageUtil.saveStringValue("Band$i", level.toString())
                    eq!!.setBandLevel(i.toShort(), level.toShort())
                } else {
                    if (level.equals("empty", true)) {
                        level = 0.toString()
                    }
                    try {
                        eq!!.setBandLevel(i.toShort(), level?.toShort()!!)
                    }catch (e: Exception){

                    }
                }
            } else {
                eq!!.setBandLevel(i.toShort(), 0.toShort())
            }
        }
        return eq
    }

    private fun updateEnabled(eq: Equalizer){
        val enabled = storageUtil.loadStringValue("Enabled")
        if (enabled == "empty" || enabled!! == 0.toString()){
            eq.enabled = false
            storageUtil.saveStringValue("Enabled", 0.toString())
        } else {
            eq.enabled = true
            storageUtil.saveStringValue("Enabled", 1.toString())
        }
    }

    companion object {

        @Volatile private var sInstance: EqModel? = null

        val instance: EqModel?
            get() {
                if (sInstance == null) {
                    synchronized(EqModel.Companion) {
                        if (sInstance == null) {
                            sInstance = EqModel()
                        }
                    }
                }
                return sInstance
            }
    }

}