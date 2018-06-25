package com.musicplayer.aow.ui.base

import android.graphics.Point
import android.support.v4.app.DialogFragment
import android.view.WindowManager


open class BaseDialogFragment : DialogFragment() {

    protected fun resizeDialogSize() {
        val window = dialog.window
        val size = Point()
        window!!.windowManager.defaultDisplay.getSize(size)
        window.setLayout((size.x * DIALOG_WIDTH_PROPORTION).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    companion object {

        private val DIALOG_WIDTH_PROPORTION = 0.85f
    }
}
