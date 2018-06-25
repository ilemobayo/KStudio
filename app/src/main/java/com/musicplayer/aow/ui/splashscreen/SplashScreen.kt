package com.musicplayer.aow.ui.splashscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import com.musicplayer.aow.ui.main.MainActivity


/**
 * Created by Arca on 10/2/2017.
 */

class SplashScreen : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this
        //permission
        startNextActivity()
    }

    private fun permission(){

        val intent = MainActivity.newIntent(applicationContext)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ActivityCompat.startActivity(this@SplashScreen, intent, null)
    }

    private fun startNextActivity()
    {
        Handler().postDelayed(
        {
            permission()
        }, timeoutMillis.toLong())
    }

    companion object {
        // Splash screen timer
        private var timeoutMillis = 1000

        var instance: SplashScreen? = null
            private set
    }
}
