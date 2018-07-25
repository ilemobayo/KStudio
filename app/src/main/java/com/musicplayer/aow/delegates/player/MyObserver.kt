package com.musicplayer.aow.delegates.player

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

class MyObserver(handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        //Write your code here
        //Whatever is written here will be
        //executed whenever a change is made
    }

}
