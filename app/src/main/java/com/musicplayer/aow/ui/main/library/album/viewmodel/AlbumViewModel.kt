package com.musicplayer.aow.ui.main.library.album.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.musicplayer.aow.delegates.data.model.Album
import com.musicplayer.aow.ui.main.library.album.model.AlbumModel

class AlbumViewModel: ViewModel(){
    private var albumModel = AlbumModel()
    var albumModelResult: MutableLiveData<ArrayList<Album>>? = MutableLiveData()

    init {
        loadAlbum()
    }

    private fun loadAlbum() {
        albumModel.getAlbum{ model, error ->
            albumModelResult?.value = model
        }
    }
}