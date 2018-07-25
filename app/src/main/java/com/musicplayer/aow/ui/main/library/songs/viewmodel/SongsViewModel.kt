package com.musicplayer.aow.ui.mvvm.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.musicplayer.aow.delegates.data.model.Song
import com.musicplayer.aow.ui.mvvm.repository.SongsRepository

class SongsViewModel: ViewModel(){
    private var songsRepo = SongsRepository()
    var songsResult: MutableLiveData<ArrayList<Song>>? = MutableLiveData()

    init {
        loadSongsFromRepository()
    }

    private fun loadSongsFromRepository() {
        songsRepo.getSongs { repos, error ->
            songsResult?.value = repos
        }
    }
}