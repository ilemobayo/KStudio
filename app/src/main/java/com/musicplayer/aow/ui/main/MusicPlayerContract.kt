package com.musicplayer.aow.ui.main

import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.delegates.player.PlayMode
import com.musicplayer.aow.delegates.player.PlaybackService
import com.musicplayer.aow.ui.base.BasePresenter
import com.musicplayer.aow.ui.base.BaseView


/* package */
interface MusicPlayerContract {

    interface View : BaseView<Presenter> {

        fun handleError(error: Throwable)

        fun onPlaybackServiceBound(service: PlaybackService)

        fun onPlaybackServiceUnbound()

        fun onSongSetAsFavorite(track: Track)

        fun onSongUpdated(track: Track?)

        fun updatePlayMode(playMode: PlayMode)

        fun updatePlayToggle(play: Boolean)

        fun updateFavoriteToggle(favorite: Boolean)
    }

    interface Presenter : BasePresenter {

        fun retrieveLastPlayMode()

        fun setSongAsFavorite(track: Track, favorite: Boolean)

        //fun bindPlaybackService()

        //fun unbindPlaybackService()
    }
}