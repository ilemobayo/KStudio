package com.musicplayer.aow.ui.main.library.songs.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.TrackDatabase
import com.musicplayer.aow.delegates.data.model.Track
import com.musicplayer.aow.ui.main.library.songs.model.SongsModel

class SongsViewModel: ViewModel(){
    private var songsModel = SongsModel()
    var songsModelResult: MutableLiveData<ArrayList<Track>>? = MutableLiveData()

    init {
        loadSongsFromRepository()
    }

    private fun loadSongsFromRepository() {
        songsModel.getSongs { model, error ->
            Log.e(this.javaClass.name, "hey i ${this.javaClass.name} is loading my data.")
            songsModelResult?.value = model
        }
    }

    fun reloadSongsFromRepository(){
        songsModel.getSongs { model, error ->
            Log.e(this.javaClass.name, "hey i ${this.javaClass.name} is re-loading my data.")
            songsModelResult?.setValue(model)
        }
    }
}