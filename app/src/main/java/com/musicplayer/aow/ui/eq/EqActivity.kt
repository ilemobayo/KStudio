package com.musicplayer.aow.ui.eq

import android.content.DialogInterface
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.jaredrummler.materialspinner.MaterialSpinner
import com.musicplayer.aow.R
import com.musicplayer.aow.application.MusicPlayerApplication
import com.musicplayer.aow.delegates.player.Player
import com.musicplayer.aow.ui.base.BaseActivity
import com.musicplayer.aow.ui.eq.model.EqModel
import com.musicplayer.aow.ui.widget.circularseekbar.CircularSeekBar
import com.musicplayer.aow.ui.widget.knob.RoundKnobButton
import com.musicplayer.aow.utils.StorageUtil
import com.readystatesoftware.systembartint.SystemBarTintManager
import kotlinx.android.synthetic.main.activity_eq.*


class EqActivity : BaseActivity(), SeekBar.OnSeekBarChangeListener,
        CircularSeekBar.OnCircularSeekBarChangeListener,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener
{

    val storageUtil = StorageUtil()
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
        return false
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
        toolbar.title = "Equalizer FX"
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        eq = EqModel.instance?.updateEqualizer(Player.instance?.mPlayer?.audioSessionId!!)

        enabled = findViewById(R.id.enabled)
        enabled!!.setOnCheckedChangeListener (this)

        flat = findViewById(R.id.flat)
        flat!!.setOnClickListener(this)

        bass_boost = findViewById(R.id.bass_boost)
        bass_boost!!.setOnSeekBarChangeListener(this)
        bass_boost_label =  findViewById(R.id.bass_boost_label)

        setUpKnob(true, bass_knob, bass_knob_value, 150, 150)

        virtualizer = findViewById(R.id.virtualizer_seekbar)
        virtualizer!!.setOnSeekBarChangeListener(this)
        setUpKnob(false, virtualizer_knob, virtualizer_knob_value, 150, 150)

        reverb = findViewById(R.id.reverb)
        reverb!!.setItems("None", "Large Hall", "Large Room", "Medium Hall", "Medium Room", "Small Room", "Plate")
        reverb!!.setOnItemSelectedListener({ view, position, id, item ->
                if (position==0) {
                    storageUtil.saveStringValue("reverb", 0.toString())
                    rev!!.preset = PresetReverb.PRESET_NONE
                    rev!!.release()
                } else if (position==1) {
                    storageUtil.saveStringValue("reverb", 1.toString())
                    rev!!.preset = PresetReverb.PRESET_LARGEHALL
                } else if (position==2) {
                    storageUtil.saveStringValue("reverb", 2.toString())
                    rev!!.preset = PresetReverb.PRESET_LARGEROOM
                } else if (position==3) {
                    storageUtil.saveStringValue("reverb", 3.toString())
                    rev!!.preset = PresetReverb.PRESET_MEDIUMHALL
                } else if (position==4) {
                    storageUtil.saveStringValue("reverb", 4.toString())
                    rev!!.preset = PresetReverb.PRESET_MEDIUMROOM
                } else if (position==5) {
                    storageUtil.saveStringValue("reverb", 5.toString())
                    rev!!.preset = PresetReverb.PRESET_SMALLROOM
                } else if (position==6) {
                    storageUtil.saveStringValue("reverb", 6.toString())
                    rev!!.preset = PresetReverb.PRESET_PLATE
                }
                 rev!!.enabled = true
        })

        sliders[0] = findViewById(R.id.slider_1)
        slider_labels[0] = findViewById(R.id.slider_label_1)
        slider_level[0] = findViewById(R.id.slider_level_1)
        sliders[1] = findViewById(R.id.slider_2)
        slider_labels[1] = findViewById(R.id.slider_label_2)
        slider_level[1] = findViewById(R.id.slider_level_2)
        sliders[2] = findViewById(R.id.slider_3)
        slider_labels[2] = findViewById(R.id.slider_label_3)
        slider_level[2] = findViewById(R.id.slider_level_3)
        sliders[3] = findViewById(R.id.slider_4)
        slider_labels[3] = findViewById(R.id.slider_label_4)
        slider_level[3] = findViewById(R.id.slider_level_4)
        sliders[4] = findViewById(R.id.slider_5)
        slider_labels[4] = findViewById(R.id.slider_label_5)
        slider_level[4] = findViewById(R.id.slider_level_5)


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
        try {
            rev = MusicPlayerApplication.instance!!.getReverb()
        }catch (ex:Exception){
            
        }
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
    override fun onProgressChanged(seekBar: SeekBar, level: Int, fromTouch: Boolean) {
            if (eq != null) {
            val new_level = min_level + (max_level - min_level) * level / 100

            for (i in 0 until num_sliders) {
                if (sliders[i] === seekBar) {
                    if (new_level == 16) {
                        slider_level[i]!!.text = "0 dB"
                    } else if (new_level < 16) {
                        if (new_level == 0) {
                            slider_level[i]!!.text = "-15 dB"
                        } else {
                            slider_level[i]!!.text = "-${16 - new_level} dB"
                        }
                    } else if (new_level > 16) {
                        slider_level[i]!!.text = "+${new_level - 16} dB"
                    }
                    eq!!.setBandLevel(i.toShort(), new_level.toShort())
                    storageUtil.saveStringValue("Band$i", new_level.toString())
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
    private fun formatBandLabel(band: IntArray): String {
        //return milliHzToString(band[0]) + "-" + milliHzToString(band[1])
        return milliHzToString(band[0])
    }

    /*=============================================================================
    milliHzToString
    =============================================================================*/
    private fun milliHzToString(milliHz: Int): String {
        if (milliHz < 1000) return ""
        return if (milliHz < 1000000)
            "" + milliHz / 1000 + "Hz"
        else
            "" + milliHz / 1000000 + "kHz"
    }

    /*=============================================================================
    updateSliders
    =============================================================================*/
    private fun updateSliders() {
        for (i in 0 until num_sliders) {
            var level = storageUtil.loadStringValue("Band$i")

            if (eq != null)
                if(level.equals("empty", true)){
                    level = eq!!.getBandLevel(i.toShort()).toString()
                    storageUtil.saveStringValue("Band$i", level.toString())
                }
            else
                if(level.equals("empty", true)){
                    level = 0.toString()
                }
            val pos = 100 * level?.toInt()!! / (max_level - min_level) + 50
            sliders[i]!!.progress = pos
        }
    }


    /*=============================================================================
    updateBassBoost
    =============================================================================*/
    private fun updateBassBoost() {
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
            if (isChecked){
                storageUtil.saveStringValue("Enabled", 1.toString())
            } else {
                storageUtil.saveStringValue("Enabled", 0.toString())
            }
            val enabled = storageUtil.loadStringValue("Enabled")
            if (enabled == "empty" || enabled!! == 0.toString()){
                eq!!.enabled = false
                storageUtil.saveStringValue("Enabled", 0.toString())
            } else {
                eq!!.enabled = true
                storageUtil.saveStringValue("Enabled", 1.toString())
            }
        }
    }

    private fun updateEnabled(): Boolean {
        val enabled = storageUtil.loadStringValue("Enabled")
        if (enabled == "empty" || enabled!! == 0.toString()){
            eq!!.enabled = false
            storageUtil.saveStringValue("Enabled", 0.toString())
            return eq!!.enabled
        } else {
            eq!!.enabled = true
            storageUtil.saveStringValue("Enabled", 1.toString())
            return eq!!.enabled
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
        if (eq != null) {
            enabled!!.isChecked = updateEnabled()
        }
    }

    /*=============================================================================
    setFlat
    =============================================================================*/
    private fun setFlat() {
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
    private fun showAbout() {
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

    private fun testband(){
        for (i in 0 until num_sliders) {
            var level = storageUtil.loadStringValue("Band$i")

            if (eq != null) {
                if (level.equals("empty", true)) {
                    level = eq!!.getBandLevel(i.toShort()).toString()
                    storageUtil.saveStringValue("Band$i", level.toString())
                }
            } else {
                Log.e(this.javaClass.name, "band$i level is empty")
            }
        }
    }

    private fun setUpKnob(bassBoost: Boolean = true, panel: RelativeLayout, txt: TextView, h: Int, w: Int){
        val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)

        val rv = RoundKnobButton(this)
        panel.addView(rv, lp)

        rv.setRotorPercentage(100)
        rv.setM_nWidth(w)
        rv.setM_nHeight(h)
        rv.redraw(this)
        rv.SetListener(object : RoundKnobButton.RoundKnobButtonListener {
            override fun onStateChange(newstate: Boolean) {

            }

            override fun onRotate(percentage: Int) {
                if (bassBoost){
                    bb!!.enabled = percentage > 0
                    bb!!.setStrength((percentage * 10).toShort())
                } else {
                    bb!!.enabled = percentage > 0
                    bb!!.setStrength((percentage * 10).toShort())
                }
                txt.text = "$percentage%"
            }
        })
    }

}
