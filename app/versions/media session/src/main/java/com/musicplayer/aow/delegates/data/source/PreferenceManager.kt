package com.musicplayer.aow.delegates.data.source

import android.content.Context
import android.content.SharedPreferences
import com.musicplayer.aow.delegates.player.PlayMode


object PreferenceManager {

    private val PREFS_NAME = "config.xml"

    /**
     * For deciding whether to add the default folders(SDCard/Download/Music),
     * if it's being deleted manually by users, then they should not be auto-recreated.
     * [.isFirstQueryFolders], [.reportFirstQueryFolders]
     */
    private val KEY_FOLDERS_FIRST_QUERY = "firstQueryFolders"

    /**
     * Play mode from the last time.
     */
    private val KEY_PLAY_MODE = "playMode"

    private fun preferences(context: Context?): SharedPreferences {
        return context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun edit(context: Context?): SharedPreferences.Editor {
        return preferences(context).edit()
    }

    /**
     * [.KEY_FOLDERS_FIRST_QUERY]
     */
    fun isFirstQueryFolders(context: Context?): Boolean {
        return preferences(context).getBoolean(KEY_FOLDERS_FIRST_QUERY, true)
    }

    /**
     * [.KEY_FOLDERS_FIRST_QUERY]
     */
    fun reportFirstQueryFolders(context: Context?) {
        edit(context).putBoolean(KEY_FOLDERS_FIRST_QUERY, false).commit()
    }

    /**
     * [.KEY_PLAY_MODE]
     */
    fun lastPlayMode(context: Context): PlayMode {
        val playModeName = preferences(context).getString(KEY_PLAY_MODE, null)
        return if (playModeName != null) {
            PlayMode.valueOf(playModeName)
        } else PlayMode.LOOP
    }

    /**
     * [.KEY_PLAY_MODE]
     */
    fun setPlayMode(context: Context, playMode: PlayMode) {
        edit(context).putString(KEY_PLAY_MODE, playMode.name).commit()
    }

}
