package com.musicplayer.aow.ui.base

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.utils.ViewUtils
import com.readystatesoftware.systembartint.SystemBarTintManager
import org.jetbrains.anko.contentView
import org.jetbrains.anko.withAlpha
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            //window.statusBarColor = Color.WHITE
            //setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            //window.statusBarColor = Color.TRANSPARENT
        } else {
//            //GPU overdraw optimaization
//            //window.setBackgroundDrawable(null)
//            val tintManager = SystemBarTintManager(this)
//            // enable status bar tint
//            tintManager.isStatusBarTintEnabled = false
//            // enable navigation bar tint
//            tintManager.setNavigationBarTintEnabled(true)
//
//            // set a custom tint color for all system bars
//            tintManager.setTintColor(R.color.white)
//            // set a custom navigation bar resource
//            tintManager.setNavigationBarTintResource(R.drawable.gradient_warning)
//            // set a custom status bar drawable
//            tintManager.setStatusBarTintResource(R.color.white)
        }

//        if (Build.VERSION.SDK_INT in 19..20) {
//            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
//            window.statusBarColor = Color.TRANSPARENT
//        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    /**
     * An easy way to set up non-home(no back button on the toolbar) activity to enable
     * go back action.
     *
     * @param toolbar The toolbar with go back button
     * @return ActionBar
     */
    protected fun supportActionBar(toolbar: Toolbar?): ActionBar? {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
        return actionBar
    }

}
