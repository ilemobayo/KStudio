package com.musicplayer.aow.delegates.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(tableName = "artist")
class Artists{

    var id: Int = 0

    var artist_id: String? = null

    @NonNull
    @PrimaryKey(autoGenerate = false)
    var artist_name: String? = "Unknown"

    var numberOfSongs: String? = null

    var numberOfAlbums: String? = null

}
