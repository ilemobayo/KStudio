package com.musicplayer.aow.ui.about

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.base.BaseActivity

class AboutUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black, resources.newTheme())
        toolbar.setNavigationOnClickListener{
            finish()
        }
    }
}
