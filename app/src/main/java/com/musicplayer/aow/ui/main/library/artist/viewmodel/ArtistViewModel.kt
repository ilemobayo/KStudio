package com.musicplayer.aow.ui.main.library.artist.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.musicplayer.aow.delegates.data.model.Artists
import com.musicplayer.aow.ui.main.library.artist.model.ArtistModel

class ArtistViewModel: ViewModel() {
    private var artistModel = ArtistModel()
    var artistModelResult: MutableLiveData<ArrayList<Artists>>? = MutableLiveData()

    init {
        loadArtist()
    }

    private fun loadArtist() {
        artistModel.getArtist{ model, error ->
            artistModelResult?.value = model
        }
    }
}