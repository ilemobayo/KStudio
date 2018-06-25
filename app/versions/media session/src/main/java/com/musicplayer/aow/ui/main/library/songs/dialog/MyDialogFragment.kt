package com.musicplayer.aow.ui.main.library.songs.dialog

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ListView
import com.musicplayer.aow.R


/**
 * Created by Arca on 1/23/2018.
 */
class MyDialogFragment: DialogFragment() {

    var listitems = arrayListOf( "item01", "item02", "item03", "item04" )

    var mylist: ListView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.custom_dialog_select_playlist, container)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return view
    }

}