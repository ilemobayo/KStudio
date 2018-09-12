package com.musicplayer.aow.delegates.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(tableName = "album")
class Album{

    var id: Int = 0

    var album_id: String? = null

    var title: String? = null

    @NonNull
    @PrimaryKey(autoGenerate = false)
    var albumName: String? = "unknown"

    var numberOfSongs: String? = null

    var numberOfSongsForArtists: String? = null

    var albumArt: String? = null

    var albumKey: String? = null

    var artist: String? = null

    var count: String? = null

}
