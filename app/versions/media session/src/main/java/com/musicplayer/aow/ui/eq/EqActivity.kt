package com.musicplayer.aow.ui.eq

import android.content.DialogInterface
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.jaredrummler.materialspinner.MaterialSpinner
import com.musicplayer.aow.R
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.ui.widget.circularseekbar.CircularSeekBar


class EqActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
        CircularSeekBar.OnCircularSeekBarChangeListener,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener
{

    var bass_boost_label: TextView? = null
    var bass_boost: CircularSeekBar? = null
    var virtualizer: CircularSeekBar? = null
    var reverb: MaterialSpinner? = null
    var enabled: CheckBox? = null
    var flat: Button? = null

    var eq: Equalizer? = null
    var bb: BassBoost? = null
    var rev: PresetReverb? = null
    var vrt: Virtualizer? = null

    var min_level = 0
    var max_level = 100

    val MAX_SLIDERS = 5 // Must match the XML layout
    var sliders = arrayOfNulls<SeekBar>(MAX_SLIDERS)
    var slider_level = arrayOfNulls<TextView>(MAX_SLIDERS)
    var slider_labels = arrayOfNulls<TextView>(MAX_SLIDERS)
    var num_sliders = 0

    /*=============================================================================
    onCreate
    =============================================================================*/
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /*=============================================================================
    onCreate
    =============================================================================*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eq)

        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.title = "EQ."
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        enabled = findViewById<CheckBox>(R.id.enabled)
        enabled!!.setOnCheckedChangeListener (this)

        flat = findViewById<Button>(R.id.flat)
        flat!!.setOnClickListener(this)

        bass_boost = findViewById<CircularSeekBar>(R.id.bass_boost)
        bass_boost!!.setOnSeekBarChangeListener(this)
        bass_boost_label =  findViewById<TextView>(R.id.bass_boost_label)

        virtualizer = findViewById<CircularSeekBar>(R.id.virtualizer_seekbar)
        virtualizer!!.setOnSeekBarChangeListener(this)

        reverb = findViewById<MaterialSpinner>(R.id.reverb)
        reverb!!.setItems("None", "Large Hall", "Large Room", "Medium Hall", "Medium Room", "Small Room", "Plate")
        reverb!!.setOnItemSelectedListener({ view, position, id, item ->

                if (position==0) {
                    rev!!.preset = PresetReverb.PRESET_NONE
                } else if (position==1) {
                    rev!!.preset = PresetReverb.PRESET_LARGEHALL
                } else if (position==2) {
                    rev!!.preset = PresetReverb.PRESET_LARGEROOM
                } else if (position==3) {
                    rev!!.preset = PresetReverb.PRESET_MEDIUMHALL
                } else if (position==4) {
                    rev!!.preset = PresetReverb.PRESET_MEDIUMROOM
                } else if (position==5) {
                    rev!!.preset = PresetReverb.PRESET_SMALLROOM
                } else if (position==6) {
                    rev!!.preset = PresetReverb.PRESET_PLATE
                }

            Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show()
        })

        sliders[0] = findViewById<SeekBar>(R.id.slider_1)
        slider_labels[0] = findViewById<TextView>(R.id.slider_label_1)
        slider_level[0] = findViewById<TextView>(R.id.slider_level_1)
        sliders[1] = findViewById<SeekBar>(R.id.slider_2)
        slider_labels[1] = findViewById<TextView>(R.id.slider_label_2)
        slider_level[1] = findViewById<TextView>(R.id.slider_level_2)
        sliders[2] = findViewById<SeekBar>(R.id.slider_3)
        slider_labels[2] = findViewById<TextView>(R.id.slider_label_3)
        slider_level[2] = findViewById<TextView>(R.id.slider_level_3)
        sliders[3] = findViewById<SeekBar>(R.id.slider_4)
        slider_labels[3] = findViewById<TextView>(R.id.slider_label_4)
        slider_level[3] = findViewById<TextView>(R.id.slider_level_4)
        sliders[4] = findViewById<SeekBar>(R.id.slider_5)
        slider_labels[4] = findViewById<TextView>(R.id.slider_label_5)
        slider_level[4] = findViewById<TextView>(R.id.slider_level_5)

        eq = MusicPlayerApplication.instance!!.getEq()
        if (eq != null) {
            eq!!.enabled = true
            val num_bands = eq!!.numberOfBands
            num_sliders = num_bands.toInt()
            val r = eq!!.bandLevelRange
            min_level = r[0].toInt()
            max_level = r[1].toInt()
            var i = 0
            while (i < num_sliders && i < MAX_SLIDERS) {
                val freq_range = eq!!.getBandFreqRange(i.toShort())
                sliders[i]!!.setOnSeekBarChangeListener(this)
                slider_labels[i]!!.text = formatBandLabel(freq_range)

                //level meter
                slider_level[i]!!.visibility = View.GONE
//                val new_level = sliders[i]!!.progress
//                if (new_level === 16) {
//                    slider_level[i]!!.text = "0 dB"
//                } else if (new_level < 16) {
//
//                    if (new_level === 0) {
//                        slider_level[i]!!.text = "-" + "15 dB"
//                    } else {
//                        slider_level[i]!!.text = "-" + (16 - new_level) + " dB"
//                    }
//
//                } else if (new_level > 16) {
//                    slider_level[i]!!.text = "+" + (new_level - 16) + " dB"
//                }
                i++
            }
        }
        for (i in num_sliders until MAX_SLIDERS) {
            sliders[i]!!.visibility = View.GONE
            slider_labels[i]!!.visibility = View.GONE
            slider_level[i]!!.visibility = View.GONE
        }

        bb = MusicPlayerApplication.instance!!.getBassBoost()
        if (bb != null) {
        } else {
            bass_boost!!.visibility = View.GONE
            bass_boost_label!!.visibility = View.GONE
        }

        vrt = MusicPlayerApplication.instance!!.getVirtualizer()
        if (vrt != null){
        }else {
            virtualizer!!.visibility = View.GONE
        }

        rev = MusicPlayerApplication.instance!!.getReverb()
        if (rev != null){
        }else {
            reverb!!.visibility = View.GONE
        }

        updateUI()
    }

    //for bass boost
    override fun onProgressChanged(circularSeekBar: CircularSeekBar, progress: Int, fromUser: Boolean) {
        if (circularSeekBar === bass_boost) {
            bb!!.enabled = progress > 0
            bb!!.setStrength(progress.toShort()) // Already in the right range 0-1000
        }else if(circularSeekBar === virtualizer){
            vrt!!.enabled = progress > 0
            vrt!!.setStrength(progress.toShort())
        }
    }

    override fun onStopTrackingTouch(seekBar: CircularSeekBar) {}

    override fun onStartTrackingTouch(seekBar: CircularSeekBar) {}

    /*=============================================================================
    onProgressChanged
    =============================================================================*/
    override fun onProgressChanged(seekBar: SeekBar, level: Int,
                                   fromTouch: Boolean) {
            if (eq != null) {
            val new_level = min_level + (max_level - min_level) * level / 100

            for (i in 0 until num_sliders) {
                if (sliders[i] === seekBar) {
                    if (new_level === 16) {
                        slider_level[i]!!.text = "0 dB"
                    } else if (new_level < 16) {

                        if (new_level === 0) {
                            slider_level[i]!!.text = "-" + "15 dB"
                        } else {
                            slider_level[i]!!.text = "-" + (16 - new_level) + " dB"
                        }

                    } else if (new_level > 16) {
                        slider_level[i]!!.text = "+" + (new_level - 16) + " dB"
                    }
                    eq!!.setBandLevel(i.toShort(), new_level.toShort())
                    break
                }
            }
        }
    }


    /*=============================================================================
    onStartTrackingTouch
    =============================================================================*/
    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    /*=============================================================================
    onStopTrackingTouch
    =============================================================================*/
    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    /*=============================================================================
    formatBandLabel
    =============================================================================*/
    fun formatBandLabel(band: IntArray): String {
        //return milliHzToString(band[0]) + "-" + milliHzToString(band[1])
        return milliHzToString(band[0])
    }

    /*=============================================================================
    milliHzToString
    =============================================================================*/
    fun milliHzToString(milliHz: Int): String {
        if (milliHz < 1000) return ""
        return if (milliHz < 1000000)
            "" + milliHz / 1000 + "Hz"
        else
            "" + milliHz / 1000000 + "kHz"
    }

    /*=============================================================================
    updateSliders
    =============================================================================*/
    fun updateSliders() {
        for (i in 0 until num_sliders) {
            val level: Int
            if (eq != null)
                level = eq!!.getBandLevel(i.toShort()).toInt()
            else
                level = 0
            val pos = 100 * level / (max_level - min_level) + 50
            sliders[i]!!.progress = pos
        }
    }


    /*=============================================================================
    updateBassBoost
    =============================================================================*/
    fun updateBassBoost() {
        if (bb != null)
            bass_boost!!.progress = bb!!.roundedStrength.toInt()
        else
            bass_boost!!.progress = 0
    }

    /*=============================================================================
    onCheckedChange
    =============================================================================*/
    override fun onCheckedChanged(view: CompoundButton, isChecked: Boolean) {
        if (view === enabled as View) {
            eq!!.enabled = isChecked
        }
    }

    /*=============================================================================
    onClick
    =============================================================================*/
    override fun onClick(view: View) {
        if (view === flat as View) {
            setFlat()
        }
    }

    /*=============================================================================
    updateUI
    =============================================================================*/
    fun updateUI() {
        updateSliders()
        updateBassBoost()
        enabled!!.isChecked = eq!!.enabled
    }

    /*=============================================================================
    setFlat
    =============================================================================*/
    fun setFlat() {
        if (eq != null) {
            for (i in 0 until num_sliders) {
                eq!!.setBandLevel(i.toShort(), 0.toShort())
            }
        }

        if (bb != null) {
            bb!!.enabled = false
            bb!!.setStrength(0.toShort())
        }

        updateUI()
    }

    /*=============================================================================
    showAbout
    =============================================================================*/
    fun showAbout() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle("About Simple EQ")
        alertDialogBuilder.setMessage(R.string.copyright_message)
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { dialog, id -> })
        val ad = alertDialogBuilder.create()
        ad.show()

    }

    /*=============================================================================
    onOptionsItemSelected
    =============================================================================*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                showAbout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
