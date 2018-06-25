package com.musicplayer.aow.utils

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.widget.CharacterDrawable

object ViewUtils {

    fun setLightStatusBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
    }

    fun clearLightStatusBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            view.systemUiVisibility = flags
        }
    }

    fun generateAlbumDrawable(context: Context?, albumName: String?): CharacterDrawable? {
        return if (context == null || albumName == null) null else CharacterDrawable.Builder()
                .setCharacter(if (albumName.length == 0) ' ' else albumName[0])
                .setBackgroundColor(ContextCompat.getColor(context, R.color.mp_characterView_background))
                .setCharacterTextColor(ContextCompat.getColor(context, R.color.mp_characterView_textColor))
                .setCharacterPadding(context.resources.getDimensionPixelSize(R.dimen.mp_characterView_padding).toFloat())
                .build()
    }
}
